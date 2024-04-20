package com.meena.k8sdeploymentflipper;

import com.meena.k8sdeploymentflipper.customresource.DeploymentFlipper;
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
    @Dependent(type = DeploymentDependentResource.class)
  }
)
public class DeploymentFlipperReconciler
  implements Reconciler<DeploymentFlipper>, ErrorStatusHandler<DeploymentFlipper>, Cleaner<DeploymentFlipper> {

  @Override
  public UpdateControl<DeploymentFlipper> reconcile(DeploymentFlipper resource, Context<DeploymentFlipper> context) {
    System.out.println("Reconciling");
    return UpdateControl.noUpdate();
  }


  @Override
  public ErrorStatusUpdateControl<DeploymentFlipper> updateErrorStatus(
    DeploymentFlipper resource,
    Context<DeploymentFlipper> context,
    Exception e
  ) {
    System.out.println("Error occurred");
    return ErrorStatusUpdateControl.noStatusUpdate();
  }


  @Override
  public DeleteControl cleanup(DeploymentFlipper resource, Context<DeploymentFlipper> context) {
    System.out.println("Cleaning up");
    return DeleteControl.defaultDelete();
  }

}
