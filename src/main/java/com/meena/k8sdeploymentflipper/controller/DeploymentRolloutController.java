package com.meena.k8sdeploymentflipper.controller;

import com.meena.k8sdeploymentflipper.customresource.DeploymentFlipper;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.RollableScalableResource;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeploymentRolloutController {

  private static final Logger log = LoggerFactory.getLogger(DeploymentRolloutController.class);
  private final KubernetesClient kubernetesClient;


  public DeploymentRolloutController(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }


  @PostMapping("/rollout-restart/{namespace}/{ownername}")
  public String rolloutRestart(
    @PathVariable String namespace,
    @PathVariable String ownername
  ) {
    log.info("Rolling out the deployment for namespace {} and ownername {}", namespace, ownername);

    // get crd of type DeploymentFlipper from api server using kubernetesclient with the owner as ownername in the given namespace
    MixedOperation<DeploymentFlipper, KubernetesResourceList<DeploymentFlipper>, Resource<DeploymentFlipper>>
      deploymentFlipperClient = kubernetesClient.resources(DeploymentFlipper.class);
    DeploymentFlipper deploymentFlipper = deploymentFlipperClient.inNamespace(namespace).withName(ownername).get();

    List<Resource<CustomResourceDefinition>> customResourcesList =
      kubernetesClient.apiextensions().v1().customResourceDefinitions().resources().toList();
    List<String> customResourcesNmeList = customResourcesList.stream()
      .map(resource -> resource.get().getKind() + " " + resource.get().getMetadata().getName())
      .toList();

    List<RollableScalableResource<Deployment>> deploymentsMatchingToFlipperCrd = kubernetesClient.apps()
      .deployments()
      .inNamespace(namespace)
      .withLabels(deploymentFlipper.getSpec().getLabels())
      .resources().toList();

    deploymentsMatchingToFlipperCrd.forEach(deployment -> {
      System.out.println("Rolling out the deployment " + deployment.get().getMetadata().getName());
      deployment.rolling().restart();
      System.out.println("Adding rollout reason to deployment " + deployment.get().getMetadata().getName());
      deployment.get()
        .getMetadata()
        .getAnnotations()
        .put("kubernetes.io/change-cause", "Restarted By DeploymentFlipper");
    });

    return "Rollout Restarted Successfully";
  }

}
