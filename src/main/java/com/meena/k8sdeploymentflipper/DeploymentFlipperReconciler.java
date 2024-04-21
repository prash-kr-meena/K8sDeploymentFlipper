package com.meena.k8sdeploymentflipper;

import static java.lang.String.format;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meena.k8sdeploymentflipper.customresource.DeploymentFlipper;
import com.meena.k8sdeploymentflipper.customresource.DeploymentFlipperStatus;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.batch.v1.CronJob;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Replaceable;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.RollableScalableResource;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@ControllerConfiguration(name = "flipper-controller")
public class DeploymentFlipperReconciler
  implements Reconciler<DeploymentFlipper>, ErrorStatusHandler<DeploymentFlipper>,
  EventSourceInitializer<DeploymentFlipper>, Cleaner<DeploymentFlipper> {

  private static final Logger log = LoggerFactory.getLogger(DeploymentFlipperReconciler.class);
  // we can get it from spring context by autowiring later
  private final KubernetesClient kubernetesClient;


  public DeploymentFlipperReconciler(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
    //    kubernetesClient.apps().deployments()
    //      .inNamespace(givenNamespaceFromAPICall)
  }


  @Override
  public ErrorStatusUpdateControl<DeploymentFlipper> updateErrorStatus(
    DeploymentFlipper resource,
    Context<DeploymentFlipper> context,
    Exception exception
  ) {
    System.out.println("Error occurred");
    return ErrorStatusUpdateControl.noStatusUpdate();
  }


  @Override
  public Map<String, EventSource> prepareEventSources(EventSourceContext<DeploymentFlipper> context) {
    final SecondaryToPrimaryMapper<Deployment> flipperMatchingDeploymentLabels =
      (Deployment deployment) -> context.getPrimaryCache()
        .list(flipper -> flipper.getSpec().getLabels().equals(deployment.getMetadata().getLabels()))
        .map(ResourceID::fromResource)
        .collect(Collectors.toSet());

    InformerEventSource<Deployment, DeploymentFlipper> deploymentEventSource = new InformerEventSource<>(
      InformerConfiguration.from(Deployment.class, context)
        .withSecondaryToPrimaryMapper(flipperMatchingDeploymentLabels)
        .build(), context
    );
    // ----------------

    InformerEventSource<CronJob, DeploymentFlipper> cronJobEventSource = new InformerEventSource<>(
      InformerConfiguration.from(CronJob.class, context)
        .withLabelSelector("app.kubernetes.io/managed-by=deployment-flipper")
        //        .withSecondaryToPrimaryMapper()
        .build(), context
    );
    // why don't we have a event source for DeploymentFlipper itself?

    return EventSourceInitializer.nameEventSources(deploymentEventSource, cronJobEventSource);
    //    return Map.of();
  }


  @Override
  public UpdateControl<DeploymentFlipper> reconcile(DeploymentFlipper flipper, Context<DeploymentFlipper> context)
    throws JsonProcessingException {

    log.info("Start Reconciling Main Resource - DeploymentFlipper {}", flipper);

    String namespace = flipper.getMetadata().getNamespace();
    String cronJobName = flipper.getMetadata().getName() + "-cronjob";
    String deploymentName = flipper.getMetadata().getName();

    Set<Deployment> secondaryDeploymentResources = context.getSecondaryResources(Deployment.class);
    Set<Deployment> deploymentsMatchingFlipper = secondaryDeploymentResources.stream()
      .filter(deployment -> deployment.getMetadata().getLabels().equals(flipper.getSpec().getLabels()))
      .collect(Collectors.toSet());

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

    //    DeploymentFlipperStatus status = new DeploymentFlipperStatus();
    //    status.setLastSync(LocalDateTime.now().toString());
    //    status.setMessage("No matching deployment Found");
    //    flipper.setStatus(status);
    //    return UpdateControl.patchStatus(flipper);
    log.info("No matching deployment Found");
    return UpdateControl.noUpdate();
  }


  private boolean match(CronJob desiredCronJob, CronJob previousCronJob) {
    if (previousCronJob == null) {
      return false;
    }
    return previousCronJob.getMetadata().getName().equals(desiredCronJob.getMetadata().getName())
      && previousCronJob.getSpec().getSchedule().equals(desiredCronJob.getSpec().getSchedule())
      && previousCronJob.getMetadata().getLabels().equals(desiredCronJob.getMetadata().getLabels());
    // add more validation checks here # todo

    // their name should be same
    // thier content should be same?  cron time (schedule)
    // the whole template
  }


  private CronJob makeDesiredCronJob(String namespace, String cronJobName, DeploymentFlipper flipper) {
    CronJob cronJob = ReconcilerUtils.loadYaml(CronJob.class, getClass(), "cronjob.yaml");
    cronJob.getMetadata().setName(cronJobName);
    cronJob.getMetadata().setNamespace(namespace);
    cronJob.getMetadata().setLabels(Map.of(
        "app.kubernetes.io/managed-by", "deployment-flipper",
        "app.kubernetes.io/owner-name", flipper.getMetadata().getName()
      )
    );
    cronJob.addOwnerReference(flipper);

    // set more things : todo
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

}
