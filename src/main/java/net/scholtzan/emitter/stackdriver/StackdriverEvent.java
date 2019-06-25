package net.scholtzan.emitter.stackdriver;

import com.google.api.Metric;
import com.google.common.base.Preconditions;
import com.google.monitoring.v3.Point;
import com.google.monitoring.v3.TimeInterval;
import com.google.monitoring.v3.TypedValue;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.Timestamps;
import org.jcodings.util.Hash;

import javax.validation.constraints.NotNull;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class StackdriverEvent {
    private final String eventPath;
    private final Number value;
    private final long timestamp;   // timestamp in milliseconds
    private final HashMap<String, String> metricLabels;

    public StackdriverEvent(
            @NotNull String eventPath,
            @NotNull Number value,
            @NotNull Long timestamp, HashMap<String,
            String> metricLabels
    ) {
        this.eventPath = Preconditions.checkNotNull(eventPath);
        this.value = Preconditions.checkNotNull(value);
        this.timestamp = Preconditions.checkNotNull(timestamp);
        this.metricLabels = metricLabels != null ? metricLabels : new HashMap<String, String>();
    }

    public String getEventPath() {
        return eventPath;
    }

    public Number getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public HashMap<String, String> getMetricLabels() {
        return metricLabels;
    }
}
