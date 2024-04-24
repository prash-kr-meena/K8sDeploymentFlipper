package com.meena.k8sdeploymentflipper;

import static com.meena.k8sdeploymentflipper.Constants.objMapper;
import static java.lang.String.format;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meena.k8sdeploymentflipper.customresource.DeploymentFlipper;
import com.meena.k8sdeploymentflipper.customresource.DeploymentFlipperStatus;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.api.model.batch.v1.CronJob;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Replaceable;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.javaoperatorsdk.operator.ReconcilerUtils;
import io.javaoperatorsdk.operator.api.config.informer.InformerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.Cleaner;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.DeleteControl;
import io.javaoperatorsdk.operator.api.reconciler.ErrorStatusHandler;
import io.javaoperatorsdk.operator.api.reconciler.ErrorStatusUpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.EventSourceContext;
import io.javaoperatorsdk.operator.api.reconciler.EventSourceInitializer;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.processing.event.ResourceID;
import io.javaoperatorsdk.operator.processing.event.source.EventSource;
import io.javaoperatorsdk.operator.processing.event.source.SecondaryToPrimaryMapper;
import io.javaoperatorsdk.operator.processing.event.source.informer.InformerEventSource;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@ControllerConfiguration(name = "flipper-controller")
public class DeploymentFlipperReconciler
  implements Reconciler<DeploymentFlipper>, ErrorStatusHandler<DeploymentFlipper>,
  EventSourceInitializer<DeploymentFlipper>, Cleaner<DeploymentFlipper> {

  private static final Logger log = LoggerFactory.getLogger(DeploymentFlipperReconciler.class);

  // we can get it from spring context by autowiring later
  private final KubernetesClient kubernetesClient;

  @Value("${k8s.service.name}")
  private String serviceName;


  public DeploymentFlipperReconciler(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }


  @Override
  public ErrorStatusUpdateControl<DeploymentFlipper> updateErrorStatus(
    DeploymentFlipper resource,
    Context<DeploymentFlipper> context,
    Exception exception
  ) {
    System.out.println("Error occurred in Controller");
    return ErrorStatusUpdateControl.noStatusUpdate();
  }


  @Override
  public Map<String, EventSource> prepareEventSources(EventSourceContext<DeploymentFlipper> context) {
    final SecondaryToPrimaryMapper<Deployment> flipperMatchingDeploymentLabels =
      (Deployment deployment) -> context.getPrimaryCache()
        .list(flipper -> {
          try {
            // each of the label in the spec of flipper exists in the deployments metadata labels and tha value is identical to the one of flippers
            Map<String, String> flipperLabels = flipper.getSpec().getLabels();
            Map<String, String> deploymentLabels = deployment.getMetadata().getLabels();
            System.out.println("EventSource - Flipper Labels: " + objMapper.writeValueAsString(flipperLabels));
            System.out.println("EventSource - Deployment Labels: " + objMapper.writeValueAsString(deploymentLabels));
            return flipperLabels.entrySet().stream().allMatch(entry ->
              deploymentLabels.containsKey(entry.getKey()) && deploymentLabels
                .get(entry.getKey())
                .equals(entry.getValue())
            );
          } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
          }
        })
        .map(ResourceID::fromResource)
        .collect(Collectors.toSet());

    InformerEventSource<Deployment, DeploymentFlipper> deploymentEventSource = new InformerEventSource<>(
      InformerConfiguration.from(Deployment.class, context)
        .withSecondaryToPrimaryMapper(flipperMatchingDeploymentLabels)
        .build(), context
    );

    InformerEventSource<CronJob, DeploymentFlipper> cronJobEventSource = new InformerEventSource<>(
      InformerConfiguration.from(CronJob.class, context)
        .withLabelSelector("app.kubernetes.io/managed-by=deployment-flipper")
        //        .withSecondaryToPrimaryMapper()
        .build(), context
    );
    // why don't we have a event source for DeploymentFlipper itself?

    return EventSourceInitializer.nameEventSources(deploymentEventSource, cronJobEventSource);
  }


  @Override
  public UpdateControl<DeploymentFlipper> reconcile(DeploymentFlipper flipper, Context<DeploymentFlipper> context)
    throws JsonProcessingException {

    log.info("Start Reconciling Main Resource - DeploymentFlipper {}", objMapper.writeValueAsString(flipper));

    String namespace = flipper.getMetadata().getNamespace();
    String cronJobName = flipper.getMetadata().getName() + "-cronjob";
    //    String deploymentName = flipper.getMetadata().getName();

    Set<Deployment> secondaryDeploymentResources = context.getSecondaryResources(Deployment.class);
    System.out.println("Secondary Deployment Resources: " + objMapper.writeValueAsString(secondaryDeploymentResources));

    if (secondaryDeploymentResources.isEmpty()) {
      log.info("No Deployment Found - in Cache, Calling Api Server");

      List<Deployment> deployments = context.getClient().apps().deployments().inNamespace(namespace)
        .withLabels(flipper.getSpec().getLabels())
        .list().getItems();

      System.out.println("Searched Deployment Resources:" + objMapper.writeValueAsString(secondaryDeploymentResources));
      secondaryDeploymentResources = new HashSet<>(deployments);
    }

    Set<Deployment> deploymentsMatchingFlipper = secondaryDeploymentResources.stream()
      .filter(deployment -> {
        // filter the ones for which all the labels present in flipper exist in deployment with the same value
        Map<String, String> flipperLabels = flipper.getSpec().getLabels();
        Map<String, String> deploymentLabels = deployment.getMetadata().getLabels();

        try {
          System.out.println("Flipper Labels: " + objMapper.writeValueAsString(flipperLabels));
          System.out.println("Deployment Labels: " + objMapper.writeValueAsString(deploymentLabels));
          return flipperLabels.entrySet().stream().allMatch(entry ->
            deploymentLabels.containsKey(entry.getKey())
              && deploymentLabels.get(entry.getKey()).equals(entry.getValue())
          );
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      })
      .collect(Collectors.toSet());
    System.out.println("Deployments Matching Flipper: " + objMapper.writeValueAsString(deploymentsMatchingFlipper));

    //    List<RollableScalableResource<Deployment>> deployments = context.getClient().apps()
    //      .deployments()
    //      .inNamespace(namespace)
    //      .resources().toList();

    // filter deployments with this primary resource

    //    List<RollableScalableResource<Deployment>> matchingDeployments =
    //      deployments.stream().filter(deployment -> deployment.get()
    //        .getMetadata().getLabels().equals(flipper.getSpec().getLabels())
    //      ).collect(Collectors.toList());

    CronJob desiredCronJob = makeDesiredCronJob(namespace, cronJobName, flipper);
    var previousCronJob = context.getSecondaryResource(CronJob.class).orElse(null);
    if (!deploymentsMatchingFlipper.isEmpty()) { // Found a deployment for which we should be having a cronjob if not present
      if (!match(desiredCronJob, previousCronJob)) {
        log.info("Creating or updating CronJob {} in {}", desiredCronJob.getMetadata().getName(), namespace);
        kubernetesClient.batch().v1().cronjobs()
          .inNamespace(namespace)
          .resource(desiredCronJob)
          .createOr(Replaceable::update);

        DeploymentFlipperStatus status = new DeploymentFlipperStatus();
        status.setLastSync(LocalDateTime.now().toString());
        status.setMessage(format("Found %s deployments matching the labels, create/update CronJob %s",
          new ObjectMapper().writeValueAsString(deploymentsMatchingFlipper), cronJobName)
        );
        flipper.setStatus(status);
        return UpdateControl.patchStatus(flipper);
      }
    }

    log.info("Making No Update to the DeploymentFlipper Resource");
    return UpdateControl.noUpdate();
  }


  private boolean match(CronJob desiredCronJob, CronJob previousCronJob) {
    if (previousCronJob == null) {
      return false;
    }
    return previousCronJob.getMetadata().getName().equals(desiredCronJob.getMetadata().getName())
      && previousCronJob.getSpec().getSchedule().equals(desiredCronJob.getSpec().getSchedule())
      && previousCronJob.getMetadata().getLabels().equals(desiredCronJob.getMetadata().getLabels());
  }


  private CronJob makeDesiredCronJob(String namespace, String cronJobName, DeploymentFlipper flipper) {
    System.out.println("Making desired Cron job, with serviceName " + serviceName);

    CronJob cronJob = ReconcilerUtils.loadYaml(CronJob.class, getClass(), "cronjob.yaml");
    cronJob.getMetadata().setName(cronJobName);
    cronJob.getMetadata().setNamespace(namespace);
    cronJob.getMetadata().setLabels(Map.of(
        "app.kubernetes.io/managed-by", "deployment-flipper",
        "app.kubernetes.io/owner-name", flipper.getMetadata().getName()
      )
    );

    // currently passing interval as cron-scheduler instead of 12h, 5m etc : Todo
    String baseUrl = MessageFormat.format("http://{0}.{1}.svc.cluster.local", serviceName, namespace);
    String controllerRolloutEndpoint = MessageFormat.format(
      "{0}/rollout-restart/{1}/{2}", baseUrl, namespace, flipper.getMetadata().getName()
    );
    String curlCommand = String.format("curl --location --request POST '%s'", controllerRolloutEndpoint);

    cronJob.getSpec().setSchedule(flipper.getSpec().getInterval());
    cronJob.getSpec()
      .getJobTemplate().getSpec()
      .getTemplate().getSpec()
      .getContainers().getFirst().getCommand()
      .set(2, curlCommand); // Setting Curl Command

    cronJob.addOwnerReference(flipper);
    return cronJob;
  }


  @Override
  public DeleteControl cleanup(DeploymentFlipper flipper, Context<DeploymentFlipper> context) {
    // here I would want to delete all the cron jobs that were created by this flipper
    System.out.println("Cleaning up");

    Set<CronJob> secondaryCronResources = context.getSecondaryResources(CronJob.class);
    List<Resource<CronJob>> secondaryResources = context.getClient().batch().v1()
      .cronjobs()
      .inNamespace(flipper.getMetadata().getNamespace())
      .withLabelSelector("app.kubernetes.io/managed-by=deployment-flipper")
      .resources().toList();

    secondaryResources.forEach(cronJob -> {
      System.out.println("Deleting cron job " + cronJob.get().getMetadata().getName());
      kubernetesClient.batch().v1().cronjobs()
        .inNamespace(cronJob.get().getMetadata().getNamespace())
        .withName(cronJob.get().getMetadata().getName())
        .delete();
    });
    return DeleteControl.defaultDelete();
  }


  public void applicationCleanup() throws JsonProcessingException {
    System.out.println("Cleaning up all the cron jobs created by the flipper");
    try (KubernetesClient kubernetesClient = new KubernetesClientBuilder().build()) {

      // read all the flipper resources and delete all the cron jobs created by them
      // get crd of type DeploymentFlipper from api server using kubernetesclient with the owner as ownername in the given namespace

      MixedOperation<DeploymentFlipper, KubernetesResourceList<DeploymentFlipper>, Resource<DeploymentFlipper>>
        deploymentFlipperClient = kubernetesClient.resources(DeploymentFlipper.class);
      List<Resource<DeploymentFlipper>> allDeploymentFlippers = deploymentFlipperClient
        .withLabel("app.kubernetes.io/managed-by", "deployment-flipper")
        .resources().toList();

      System.out.println("All Deployment Flippers: " + objMapper.writeValueAsString(allDeploymentFlippers));

      allDeploymentFlippers.forEach(flipper -> {
        try {
          System.out.println("Cleaning up for flipper: " + flipper.get().getMetadata().getName());
          // delete all the cron jobs created by this flipper
          List<Resource<CronJob>> secondaryResources = kubernetesClient.batch().v1()
            .cronjobs()
            .inNamespace(flipper.get().getMetadata().getNamespace())
            .withLabelSelector("app.kubernetes.io/managed-by=deployment-flipper")
            .resources()
            .toList();

          System.out.println("Secondary Resources: " + objMapper.writeValueAsString(secondaryResources));

          secondaryResources.forEach(cronJob -> {
            System.out.println("Deleting cron job " + cronJob.get().getMetadata().getName());
            cronJob.delete();
          });
          System.out.println("Deleted all cron jobs for flipper: " + flipper.get().getMetadata().getName());

        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      });
    }
  }

}
// TODO
// when deleteing the whole deployment of the controller application it should remove all the cronjobs created by it
// currently it only removes the cronjob when we delete a flipper