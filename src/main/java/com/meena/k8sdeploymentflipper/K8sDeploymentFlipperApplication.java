package com.meena.k8sdeploymentflipper;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class K8sDeploymentFlipperApplication {

  public static void main(String[] args) {
    SpringApplication.run(K8sDeploymentFlipperApplication.class, args);

    //    KubernetesClient client = new KubernetesClientBuilder().build();
    //    DeploymentFlipperReconciler deploymentFlipperReconciler = new DeploymentFlipperReconciler(client);
    //    Operator operator = new Operator();
    //    operator.register(deploymentFlipperReconciler);
    //    operator.start();

    //    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    //      System.out.println("Shutdown hook triggered, cleaning up...");
    //      try {
    //        deploymentFlipperReconciler.applicationCleanup();
    //        //        sleep(5000); // Sleep for 5 seconds
    //      } catch (JsonProcessingException e) {
    //        throw new RuntimeException(e);
    //      }
    //    }));
  }

}
