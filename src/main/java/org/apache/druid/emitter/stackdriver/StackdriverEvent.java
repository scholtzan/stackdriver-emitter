package org.apache.druid.emitter.stackdriver;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@JsonSerialize(using = StackdriverEventSerializer.class)
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
        this.eventPath = eventPath;
        this.value = value;
        this.timestamp = timestamp;
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


class StackdriverEventSerializer extends StdSerializer<StackdriverEvent> {
    public StackdriverEventSerializer() {
        this(null);
    }

    public StackdriverEventSerializer(Class<StackdriverEvent> t) {
        super(t);
    }

    @Override
    public void serialize(
            StackdriverEvent value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {
        // todo: add tests for serialization

        Date date = new Date(value.getTimestamp());
        Format format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String formattedTime = format.format(date);

        jgen.writeStartObject();

        jgen.writeObjectFieldStart("metric");
        jgen.writeStringField("type", value.getEventPath());
        jgen.writeObjectFieldStart("labels");

        for (Map.Entry<String, String> label : value.getMetricLabels().entrySet()) {
            jgen.writeStringField(label.getKey(), label.getValue());
        }

        jgen.writeEndObject();
        jgen.writeEndObject();

        // todo: should this be global?
        jgen.writeObjectFieldStart("resource");
        jgen.writeStringField("type", "global");
        jgen.writeEndObject();

        jgen.writeStringField("metricKind", "GAUGE");
        jgen.writeStringField("valueType", "INT64");

        jgen.writeObjectFieldStart("points");
        jgen.writeObjectFieldStart("interval");
        jgen.writeStringField("endTime", formattedTime);
        jgen.writeStringField("startTime", formattedTime);
        jgen.writeEndObject();
        jgen.writeEndObject();

        jgen.writeEndObject();
    }
}
