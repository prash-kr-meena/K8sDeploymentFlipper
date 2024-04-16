package com.meena.k8sdeploymentflipper.dependentresource;

import com.meena.k8sdeploymentflipper.customresource.DeploymentFlipper;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;

@KubernetesDependent(labelSelector = "app=mesh")
public class DeploymentDependentResource extends CRUDKubernetesDependentResource<Deployment, DeploymentFlipper> {

  public DeploymentDependentResource(Class<Deployment> resourceType) {
    super(resourceType);
    System.out.println("DeploymentDependentResource constructor");
  }

}
