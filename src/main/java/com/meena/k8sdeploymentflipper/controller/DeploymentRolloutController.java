package com.meena.k8sdeploymentflipper.controller;

import com.meena.k8sdeploymentflipper.customresource.DeploymentFlipper;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Gettable;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.RollableScalableResource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeploymentRolloutController {

  private static final Logger log = LoggerFactory.getLogger(DeploymentRolloutController.class);
  private final KubernetesClient kubernetesClient;


  public DeploymentRolloutController(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }


  @PostMapping("/rollout-restart/{namespace}/{ownername}")
  public void rolloutRestart(
    @PathVariable String namespace,
    @PathVariable String ownername
  ) {
    log.info("Rolling out the deployment for namespace {} and ownername {}", namespace, ownername);
    // get crd of type DeploymentFlipper from api server using kubernetesclient with the owner as ownername in the given namespace

    MixedOperation<DeploymentFlipper, KubernetesResourceList<DeploymentFlipper>, Resource<DeploymentFlipper>>
      deploymentFlipperClient = kubernetesClient.resources(DeploymentFlipper.class);
    DeploymentFlipper deploymentFlipper = deploymentFlipperClient.inNamespace(namespace).withName(ownername).get();

    List<Resource<CustomResourceDefinition>> list =
      kubernetesClient.apiextensions().v1().customResourceDefinitions().resources().toList();
    List<String> list1 =
      list.stream().map(resource -> resource.get().getKind() + " " + resource.get().getMetadata().getName()).toList();

    List<RollableScalableResource<Deployment>> list2 = kubernetesClient.apps()
      .deployments()
      .inNamespace(namespace)
      .withLabels(deploymentFlipper.getSpec().getLabels())
      .resources().toList();

    list2.forEach(deployment -> deployment.rolling().restart());

    Set<CustomResourceDefinition> flipper = kubernetesClient.apiextensions().v1().customResourceDefinitions()
      .resources()
      .filter(resource -> resource.get().getMetadata().getName().equals(ownername))
      .map(Gettable::get)
      .collect(Collectors.toSet());
    // we should get only one here

    CustomResourceDefinition crd =
      kubernetesClient.apiextensions().v1().customResourceDefinitions().withName("sparkclusters.radanalytics.io").get();
    kubernetesClient.apiextensions().v1().customResourceDefinitions().withName("Flipper.meena.com").get();
    log.info("Flipper resource {} : {}", flipper.size(), flipper);

    //    kubernetesClient.apps()
    //      .deployments().inNamespace(namespace)
    //      .withLabels()
    //      .resources()

  }

}
