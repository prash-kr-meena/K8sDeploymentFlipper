package com.meena.k8sdeploymentflipper;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.javaoperatorsdk.operator.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class K8sDeploymentFlipperApplication {

  public static void main(String[] args) {
    System.out.println("Spring Main");
    SpringApplication.run(K8sDeploymentFlipperApplication.class, args);

    KubernetesClient client = new KubernetesClientBuilder().build();
    Operator operator = new Operator();
    operator.register(new DeploymentFlipperReconciler(client));

    System.out.println("Spring Started");
  }

}
