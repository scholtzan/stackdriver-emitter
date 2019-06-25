package net.scholtzan.emitter.stackdriver;

import com.google.api.Metric;
import com.google.common.base.Preconditions;
import com.google.monitoring.v3.Point;
import com.google.monitoring.v3.TimeInterval;
import com.google.monitoring.v3.TypedValue;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.Timestamps;

import javax.validation.constraints.NotNull;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class StackdriverEvent {
    private final String eventPath;
    private final String value;
    private final long timestamp;   // timestamp in milliseconds

    public StackdriverEvent(@NotNull String eventPath, @NotNull String value, @NotNull Long timestamp) throws ParseException {
        this.eventPath = Preconditions.checkNotNull(eventPath);
        this.value = Preconditions.checkNotNull(value);
        this.timestamp = Preconditions.checkNotNull(timestamp);
    }

    public String getEventPath() {
        return eventPath;
    }

    public String getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
