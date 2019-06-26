package net.scholtzan.emitter.stackdriver;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

public class StackdriverEmitterConfig {
    @JsonProperty
    private final String hostname;
    @JsonProperty
    private final Integer port;
    @JsonProperty
    private final Integer flushThreshold;
    @JsonProperty
    private final Integer maxQueueSize;
    @JsonProperty
    private final Long consumeDelay;
    @JsonProperty
    private final String projectId;
    @JsonProperty
    private final String metricMapPath;

    @JsonCreator
    public StackdriverEmitterConfig(
            @JsonProperty("hostname") String hostname,
            @JsonProperty("port") Integer port,
            @JsonProperty("flushThreshold") Integer flushThreshold,
            @JsonProperty("maxQueueSize") Integer maxQueueSize,
            @JsonProperty("consumeDelay") Long consumeDelay,
            @JsonProperty("projectId") String projectId,
            @JsonProperty("metricMapPath") String metricMapPath
    ) {
        this.hostname = Preconditions.checkNotNull(hostname, "Stackdriver hostname cannot be null.");
        this.port = Preconditions.checkNotNull(port, "Stackdriver port cannot be null.");
        this.flushThreshold = flushThreshold != null ? flushThreshold : 0;
        this.maxQueueSize = maxQueueSize != null ? maxQueueSize : 0;
        this.consumeDelay = consumeDelay != null ? consumeDelay : 0;
        this.projectId = projectId != null ? projectId : "";
        this.metricMapPath = metricMapPath != null ? metricMapPath : "";
    }

    @JsonProperty
    public String getHost() {
        return hostname;
    }

    @JsonProperty
    public int getPort() {
        return port;
    }

    @JsonProperty
    public int getFlushThreshold() {
        return flushThreshold;
    }

    @JsonProperty
    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    @JsonProperty
    public long getConsumeDelay() {
        return consumeDelay;
    }

    @JsonProperty
    public String getProjectId() {
        return projectId;
    }

    @JsonProperty
    public String getMetricMapPath() {
        return metricMapPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof StackdriverEmitterConfig)) {
            return false;
        }

        StackdriverEmitterConfig that = (StackdriverEmitterConfig) o;

        return getPort() == that.getPort() &&
                getConsumeDelay() == that.getConsumeDelay() &&
                getFlushThreshold() == that.getFlushThreshold() &&
                getHost().equals(that.getHost()) &&
                getMaxQueueSize() == that.getMaxQueueSize() &&
                getMetricMapPath().equals(that.getMetricMapPath()) &&
                getProjectId().equals(that.getProjectId());
    }
}
