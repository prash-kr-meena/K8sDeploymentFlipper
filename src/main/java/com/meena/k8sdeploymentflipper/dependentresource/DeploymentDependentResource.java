package com.meena.k8sdeploymentflipper.dependentresource;

import com.meena.k8sdeploymentflipper.customresource.DeploymentFlipper;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDNoGCKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;

// This is dependent only for the scenarios where
// 1. when created I need to check if it had the correct label , which it would have otherwise it will not get the call here
// 2. if it is created, ie generation is 1, then I need to create a cron job corresponding to it
// 3. if it already present then   just check if the cron job is also available or not?
//    do I need to check that? as we will already be handling the cron-job deletion and will create it automatically after deletion

@KubernetesDependent()
public class DeploymentDependentResource extends CRUDNoGCKubernetesDependentResource<Deployment, DeploymentFlipper> {
  //public class DeploymentDependentResource extends CRUDKubernetesDependentResource<Deployment, DeploymentFlipper> {


  public DeploymentDependentResource() {
    super(Deployment.class);
    System.out.println("DeploymentDependentResource constructor - Done");
  }


  public DeploymentDependentResource(Class<Deployment> resourceType) {
    super(resourceType);
    System.out.println("DeploymentDependentResource constructor with resourceType - Done");
  }


  @Override
  protected Deployment desired(DeploymentFlipper primary, Context<DeploymentFlipper> context) {
    System.out.println("DeploymentDependentResource desired");
    Deployment desired = super.desired(primary, context);
    System.out.println("DeploymentDependentResource desired - Done");
    return desired;
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
    Deployment actual, Deployment desired, DeploymentFlipper primary, Context<DeploymentFlipper> context
  ) {
    System.out.println("DeploymentDependentResource update");
    Deployment update = super.update(actual, desired, primary, context);
    System.out.println("DeploymentDependentResource update - Done");
    return update;
  }


  @Override
  public void deleteTargetResource(
    DeploymentFlipper primary, Deployment resource, String key, Context<DeploymentFlipper> context
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
