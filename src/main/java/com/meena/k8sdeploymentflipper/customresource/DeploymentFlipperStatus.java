package com.meena.k8sdeploymentflipper.customresource;

import lombok.Data;

@Data
public class DeploymentFlipperStatus {

  private String lastSync;
  private String message;

}
