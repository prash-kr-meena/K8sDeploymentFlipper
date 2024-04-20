package com.meena.k8sdeploymentflipper;

import com.meena.k8sdeploymentflipper.customresource.DeploymentFlipper;
import com.meena.k8sdeploymentflipper.dependentresource.CronJobDependentResource;
import com.meena.k8sdeploymentflipper.dependentresource.DeploymentDependentResource;
import io.javaoperatorsdk.operator.api.reconciler.Cleaner;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.DeleteControl;
import io.javaoperatorsdk.operator.api.reconciler.ErrorStatusHandler;
import io.javaoperatorsdk.operator.api.reconciler.ErrorStatusUpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;
import org.springframework.stereotype.Component;

@Component
@ControllerConfiguration(
  name = "flipper-controller",
  dependents = {
    @Dependent(type = CronJobDependentResource.class),
    @Dependent(type = DeploymentDependentResource.class)
  }
)
public class DeploymentFlipperReconciler
  implements Reconciler<DeploymentFlipper>, ErrorStatusHandler<DeploymentFlipper>, Cleaner<DeploymentFlipper> {
  //  SecondaryToPrimaryMapper<Deployment>

  //  private final KubernetesClient kubernetesClient;

  //  public DeploymentFlipperReconciler(KubernetesClient kubernetesClient) {
  //    this.kubernetesClient = kubernetesClient;
  //  }


  @Override
  public UpdateControl<DeploymentFlipper> reconcile(DeploymentFlipper resource, Context<DeploymentFlipper> context) {
    //    Set<Deployment> secondaryResources = context.getSecondaryResources(Deployment.class);
    //    secondaryResources.stream().forEach(deployment -> deployment.i);

    //    context.getSecondaryResource(Deployment.class).is
    System.out.println("Start Reconciling Main Resource - DeploymentFlipper");
    return UpdateControl.noUpdate();
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
  public DeleteControl cleanup(DeploymentFlipper resource, Context<DeploymentFlipper> context) {
    // here I would want to delete all the cron jobs that were created by this flipper
    System.out.println("Cleaning up");
    return DeleteControl.defaultDelete();
  }

  //  @Override
  //  public Set<ResourceID> toPrimaryResourceIDs(Deployment resource) {
  //    return Set.of();
  //  }

}
