package net.scholtzan.emitter.stackdriver;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.List;
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


    @JsonCreator
    public StackdriverEmitterConfig(
        @JsonProperty("hostname") String hostname,
        @JsonProperty("port") Integer port,
        @JsonProperty("prefix") String prefix,
        @JsonProperty("separator") String separator,
        @JsonProperty("includeHost") Boolean includeHost
    )
    {
        this.hostname = Preconditions.checkNotNull(hostname, "StatsD hostname cannot be null.");
        this.port = Preconditions.checkNotNull(port, "StatsD port cannot be null.");
        this.prefix = prefix != null ? prefix : "";
        this.separator = separator != null ? separator : ".";
        this.includeHost = includeHost != null ? includeHost : false;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(hostname, port, prefix, separator, includeHost);
    }

    @JsonProperty
    public String getHostname()
    {
        return hostname;
    }

    @JsonProperty
    public int getPort()
    {
        return port;
    }

    @JsonProperty
    public String getPrefix()
    {
        return prefix;
    }

    @JsonProperty
    public String getSeparator()
    {
        return separator;
    }

    @JsonProperty
    public Boolean getIncludeHost()
    {
        return includeHost;
    }
}
