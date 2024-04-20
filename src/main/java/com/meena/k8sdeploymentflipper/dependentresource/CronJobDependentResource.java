package com.meena.k8sdeploymentflipper.dependentresource;

import com.meena.k8sdeploymentflipper.customresource.DeploymentFlipper;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.batch.v1.CronJob;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import java.util.Optional;

// This is dependent only for the scenarios where
// 1. when created I need to check if it had the correct label , which it would have otherwise it will not get the call here
// 2. if it is created, ie generation is 1, then I need to create a cron job corresponding to it
@KubernetesDependent()
//public class CronJobDependentResource extends CRUDNoGCKubernetesDependentResource<Deployment, DeploymentFlipper> {
public class CronJobDependentResource extends CRUDKubernetesDependentResource<CronJob, DeploymentFlipper> {

  public CronJobDependentResource() {
    super(CronJob.class);
    System.out.println("CronJobDependentResource constructor - Done");
  }


  public CronJobDependentResource(Class<CronJob> resourceType) {
    super(resourceType);
    System.out.println("CronJobDependentResource constructor with resourceType - Done");
  }


  @Override
  protected CronJob desired(DeploymentFlipper primary, Context<DeploymentFlipper> context) {
    System.out.println("CronJobDependentResource desired");
    Optional<Deployment> secondaryResource =
      context.getSecondaryResource(Deployment.class, (deploymentClass, deploymentFlipper, ctx) -> {
        //        primaryResource.getSpec().getLabels()
        return Optional.empty();
      });
    CronJob desired = super.desired(primary, context);
    System.out.println("CronJobDependentResource desired - Done");
    return desired;
  }


  @Override
  public CronJob create(CronJob desired, DeploymentFlipper primary, Context<DeploymentFlipper> context) {
    System.out.println("CronJobDependentResource create");
    CronJob deployment = super.create(desired, primary, context);
    System.out.println("CronJobDependentResource create - Done");
    return deployment;
  }


  @Override
  public CronJob update(
    CronJob actual, CronJob desired, DeploymentFlipper primary, Context<DeploymentFlipper> context
  ) {
    System.out.println("CronJobDependentResource update");
    CronJob update = super.update(actual, desired, primary, context);
    System.out.println("CronJobDependentResource update - Done");
    return update;
  }


  @Override
  public void deleteTargetResource(
    DeploymentFlipper primary, CronJob resource, String key, Context<DeploymentFlipper> context
  ) {
    System.out.println("CronJobDependentResource deleteTargetResource");
    super.deleteTargetResource(primary, resource, key, context);
  }


  @Override
  public void delete(DeploymentFlipper primary, Context<DeploymentFlipper> context) {
    System.out.println("CronJobDependentResource delete");
    super.delete(primary, context);
  }

}
