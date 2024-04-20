Write a Kubernetes (k8s) Controller to restart deployments

Problem
Often times there might be a need do a rolling restart of applications in k8s for complicance reasons (long running
applications) or to retrigger a mutating webhook (used for sidecar injection for a Service Mesh).
The task is to implement a k8s controller that does a rolling restart of deployments (basically `kubectl rollout restart
deploy <deployment-name>`) only for the deployments that have a specific label (mesh: "true") at a configured interval.

The controller should take the following Input Parameters (can be accepted either over command line or read from a
config map):

- `interval` : interval at which the matched deployments should be rotated (default is 10m)
- `namespace` : list of namespaces (default `all`)

Goals

- Demo a working controller pointed at a local k8s cluster (use kind to create a cluster)
- Working Unit tests for the controller

Stretch Goals

- Build and publish the docker image and run the controller in a k8s cluster
- Add a CRD to manage matching criteria to restart deployments.

For example the below sample CR can result in restarts of deployments in namespace mesh having a label every 12 hrs:

```yaml
apiVersion: flipper.io/v1aplha1
kind: Flipper
metadata:
  name: service-mesh-flipper
  namespace: flipper
spec:
  interval: 12h
  match:
    labels:
      mesh: "true"
  namespace: "mesh"
```