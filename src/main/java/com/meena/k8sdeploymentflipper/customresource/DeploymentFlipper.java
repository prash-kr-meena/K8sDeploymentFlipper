package com.meena.k8sdeploymentflipper.customresource;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Plural;
import io.fabric8.kubernetes.model.annotation.ShortNames;
import io.fabric8.kubernetes.model.annotation.Singular;
import io.fabric8.kubernetes.model.annotation.Version;

@Group("com.meena")
@Version("v1")
@Kind("Flipper")
@Singular("flipper")
@Plural("flippers")
@ShortNames("flp")
public class DeploymentFlipper extends CustomResource<DeploymentFlipperSpec, DeploymentFlipperStatus>
  implements Namespaced {
}
