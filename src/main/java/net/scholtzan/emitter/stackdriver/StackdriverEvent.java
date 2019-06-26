package net.scholtzan.emitter.stackdriver;

import com.google.common.base.Preconditions;

import javax.validation.constraints.NotNull;
import java.util.HashMap;

public class StackdriverEvent {
    private final String eventPath;
    private final Number value;
    private final long timestamp;   // timestamp in milliseconds
    private final HashMap<String, String> metricLabels;

    public StackdriverEvent(
            @NotNull String eventPath,
            @NotNull Number value,
            @NotNull Long timestamp,
            HashMap<String, String> metricLabels
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StackdriverEvent that = (StackdriverEvent) o;

        return getEventPath().equals(that.getEventPath()) &&
                getValue().equals(that.getValue()) &&
                getMetricLabels() == that.getMetricLabels() &&
                getTimestamp() == that.getTimestamp();
    }
}
