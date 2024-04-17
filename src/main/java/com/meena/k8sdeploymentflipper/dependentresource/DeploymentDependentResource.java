package com.meena.k8sdeploymentflipper.dependentresource;

import com.meena.k8sdeploymentflipper.customresource.DeploymentFlipper;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDNoGCKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;

@KubernetesDependent(labelSelector = "app=mesh")
public class DeploymentDependentResource extends CRUDNoGCKubernetesDependentResource<Deployment, DeploymentFlipper> {

  public DeploymentDependentResource() {
    super(Deployment.class);
    System.out.println("DeploymentDependentResource constructor - Done");
  }


  public DeploymentDependentResource(Class<Deployment> resourceType) {
    super(resourceType);
    System.out.println("DeploymentDependentResource constructor with resourceType - Done");
  }


  @Override
  public Deployment create(Deployment desired, DeploymentFlipper primary, Context<DeploymentFlipper> context) {
    System.out.println("DeploymentDependentResource create");
    Deployment deployment = super.create(desired, primary, context);
    System.out.println("DeploymentDependentResource create - Done");
    return deployment;
  }


  @Override
  public Deployment update(
    Deployment actual,
    Deployment desired,
    DeploymentFlipper primary,
    Context<DeploymentFlipper> context
  ) {
    System.out.println("DeploymentDependentResource update");
    Deployment update = super.update(actual, desired, primary, context);
    System.out.println("DeploymentDependentResource update - Done");
    return update;
  }


  @Override
  public void deleteTargetResource(
    DeploymentFlipper primary,
    Deployment resource,
    String key,
    Context<DeploymentFlipper> context
  ) {
    System.out.println("DeploymentDependentResource deleteTargetResource");
    super.deleteTargetResource(primary, resource, key, context);
  }


  @Override
  public void delete(DeploymentFlipper primary, Context<DeploymentFlipper> context) {
    System.out.println("DeploymentDependentResource delete");
    super.delete(primary, context);
  }

}
