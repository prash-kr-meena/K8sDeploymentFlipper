package com.meena.k8sdeploymentflipper.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {


  @Value("${app.version}")
  private String appVersion;

  @Value("${spring.application.name}")
  private String applicationName;

  @Value("${k8s.service.name}")
  private String serviceName;


  @GetMapping("/status")
  public String getAppVersion() {
    //    return String.format("Application: %s, Running on Version: %s ", applicationName, appVersion);
    return String.format("Application: %s, Running on Version: %s  & serviceNam %s",
      applicationName,
      appVersion,
      serviceName);
  }

}
