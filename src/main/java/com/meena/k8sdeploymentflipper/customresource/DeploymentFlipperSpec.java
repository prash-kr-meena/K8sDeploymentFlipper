package com.meena.k8sdeploymentflipper.customresource;

import java.util.Map;
import lombok.Data;

@Data
public class DeploymentFlipperSpec {

  // for now, we will just do cron expression,
  // but will see, how can we convert 12h, 30m --> into cron expression for k8s cront jobs
  private String interval;
  //  private String namespace; -> Namespace would be taken from its object itself, ie where the flipper is deployed
  //   this means that a person would have to put multiple flipper objects in different namespaces to controll them
//  mapping is q:1 1 flipper 1 namespace and 1 set of lables
  private Map<String, String> labels;

}
