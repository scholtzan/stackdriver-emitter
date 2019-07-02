package org.apache.druid.emitter.stackdriver;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.druid.java.util.common.logger.Logger;

/**
 * Plugin configuration handler.
 */
public class StackdriverEmitterConfig {
    private static final Logger log = new Logger(StackdriverEmitterConfig.class);

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
            @JsonProperty("flushThreshold") Integer flushThreshold,
            @JsonProperty("maxQueueSize") Integer maxQueueSize,
            @JsonProperty("consumeDelay") Long consumeDelay,
            @JsonProperty("projectId") String projectId,
            @JsonProperty("metricMapPath") String metricMapPath
    ) {
        this.flushThreshold = flushThreshold != null ? flushThreshold : 0;
        this.maxQueueSize = maxQueueSize != null ? maxQueueSize : 0;
        this.consumeDelay = consumeDelay != null ? consumeDelay : 0;
        this.projectId = projectId != null ? projectId : "";
        this.metricMapPath = metricMapPath != null ? metricMapPath : "";
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

        return getConsumeDelay() == that.getConsumeDelay() &&
                getFlushThreshold() == that.getFlushThreshold() &&
                getMaxQueueSize() == that.getMaxQueueSize() &&
                getMetricMapPath().equals(that.getMetricMapPath()) &&
                getProjectId().equals(that.getProjectId());
    }
}
