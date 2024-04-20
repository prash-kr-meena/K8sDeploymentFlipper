package com.meena.k8sdeploymentflipper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class K8sDeploymentFlipperApplication {

  public static void main(String[] args) {
    System.out.println("Spring Main");
    SpringApplication.run(K8sDeploymentFlipperApplication.class, args);
    System.out.println("Spring Started");
  }

}
