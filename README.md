# Stackdriver Emitter Plugin for Druid

## Getting it working in Druid

Run `mvn package` to create a `.jar` file that contains all dependencies.

The generated `.jar` file needs to be copied into druids `extensions/` directory (for example into `/app/druid/extensions/stackdriver-emitter/`).

When running druid it should automatically detect and execute this extension.

### Stackdriver Authentication in GKE

To authenticate to the Stackdriver API the `GOOGLE_APPLICATION_CREDENTIALS` environment variable needs to be set pointing to the `.json` file with the necessary credentials.

To set these credentials in GKE the following steps are necessary:

* [Create service account keys](https://cloud.google.com/iam/docs/creating-managing-service-account-keys) which should download a `.json` with the credentials
* [Create a Kubernetes secret resource for those service account credentials](https://kubernetes.io/docs/concepts/configuration/secret/): `kubectl create secret generic service-account-credentials --from-file /path/to/credentials.json  `
* [Mount the credentials in the container that needs access](https://kubernetes.io/docs/tasks/configure-pod-container/configure-volume-storage/)
* [Set the `GOOGLE_APPLICATION_CREDENTIALS` environment variable](https://kubernetes.io/docs/tasks/inject-data-application/define-environment-variable-container/)

See [https://stackoverflow.com/questions/47021469/how-to-set-google-application-credentials-on-gke-running-through-kubernetes](https://stackoverflow.com/questions/47021469/how-to-set-google-application-credentials-on-gke-running-through-kubernetes)

## Configuration

Configuration parameters for the plugin that can be set in the config file (eg. `common.runtime.properties`): 

```yaml
druid.emitter=stackdriver

# number of events sent together in one request to Stackdriver
druid.emitter.stackdriver.flushThreshold=5

# max. queue size for event queue
druid.emitter.stackdriver.maxQueueSize=1000

# frequency in ms of sending collected events to Stackdriver
druid.emitter.stackdriver.consumeDelay=60000

# Stackdriver project ID
druid.emitter.stackdriver.projectId=streaming-events-dev

# optionally, file with whitelist of druid events that should be processed
druid.emitter.stackdriver.metricMapPath=/app/druid/extensions/stackdriver-emitter/defaultMetrics.json
```

The extension must also be registered in the config file:

```yaml
druid.extensions.loadList=[..., "stackdriver-emitter", ...]
```
