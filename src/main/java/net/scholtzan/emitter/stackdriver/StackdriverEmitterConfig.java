package net.scholtzan.emitter.stackdriver;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import java.util.Objects;

public class StackdriverEmitterConfig {
    @JsonProperty
    private final String hostname;
    @JsonProperty
    private final Integer port;
    @JsonProperty
    private final String prefix;
    @JsonProperty
    private final String separator;
    @JsonProperty
    private final Boolean includeHost;
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
            @JsonProperty("prefix") String prefix,
            @JsonProperty("separator") String separator,
            @JsonProperty("includeHost") Boolean includeHost,
            @JsonProperty("flushThreshold") Integer flushThreshold,
            @JsonProperty("maxQueueSize") Integer maxQueueSize,
            @JsonProperty("consumeDelay") Long consumeDelay,
            @JsonProperty("projectId") String projectId,
            @JsonProperty("metricMapPath") String metricMapPath
    ) {
        this.hostname = Preconditions.checkNotNull(hostname, "StatsD hostname cannot be null.");
        this.port = Preconditions.checkNotNull(port, "StatsD port cannot be null.");
        this.prefix = prefix != null ? prefix : "";
        this.separator = separator != null ? separator : ".";
        this.includeHost = includeHost != null ? includeHost : false;
        this.flushThreshold = flushThreshold != null ? flushThreshold : 0;
        this.maxQueueSize = maxQueueSize != null ? maxQueueSize : 0;
        this.consumeDelay = consumeDelay != null ? consumeDelay : 0;
        this.projectId = projectId != null ? projectId : "";
        this.metricMapPath = metricMapPath != null ? metricMapPath : "";
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostname, port, prefix, separator, includeHost);
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
    public String getPrefix() {
        return prefix;
    }

    @JsonProperty
    public String getSeparator() {
        return separator;
    }

    @JsonProperty
    public Boolean getIncludeHost() {
        return includeHost;
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
}
