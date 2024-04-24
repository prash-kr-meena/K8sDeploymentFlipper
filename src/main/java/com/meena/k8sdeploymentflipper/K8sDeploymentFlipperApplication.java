package com.meena.k8sdeploymentflipper;

import static java.lang.Thread.sleep;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.javaoperatorsdk.operator.Operator;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class K8sDeploymentFlipperApplication {

  //  @Bean
  //  public Operator operator() {
  //    return new Operator();
  //  }

  @Autowired
  private Operator operator;

  //  @Autowired
  //  private DeploymentFlipperReconciler deploymentFlipperReconciler;

  //  @PostConstruct
  //  public void init() {
  //    System.out.println("Application started...");
  //    System.out.println(operator);
  //    KubernetesClient client = new KubernetesClientBuilder().build();
  //    DeploymentFlipperReconciler deploymentFlipperReconciler = new DeploymentFlipperReconciler(client);
  //    Operator operator = new Operator();
  //    operator.register(deploymentFlipperReconciler);
  //    operator.start();

  //    operator.register(deploymentFlipperReconciler);
  //    operator.start();
  //  }


  public static void main(String[] args) {
    SpringApplication.run(K8sDeploymentFlipperApplication.class, args);

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
