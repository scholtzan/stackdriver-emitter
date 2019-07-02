# Stackdriver Emitter Plugin for Druid

## Getting it working in Druid




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
