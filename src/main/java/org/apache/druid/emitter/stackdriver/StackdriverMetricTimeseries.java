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
import java.util.*;

/**
 * Representation of emitted events as collection of data points that describes the time-varying values of a metric.
 */
@JsonSerialize(using = StackdriverMetricTimeseriesSerializer.class)
public class StackdriverMetricTimeseries {
    private final String metricType;
    private final HashMap<String, String> metricLabels;
    private List<Point> points;

    StackdriverMetricTimeseries(
            @NotNull String eventPath,
            @NotNull Number value,
            @NotNull Long timestamp,
            HashMap<String, String> metricLabels
    ) {
        this.metricType = eventPath;
        this.points = Collections.singletonList(new Point(timestamp, value));
        this.metricLabels = metricLabels != null ? metricLabels : new HashMap<String, String>();
    }

    String getMetricType() {
        return metricType;
    }

    HashMap<String, String> getMetricLabels() {
        return metricLabels;
    }

    void addPoints(List<Point> points) {
        this.points.addAll(points);
    }

    List<Point> getPoints() {
        return points;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StackdriverMetricTimeseries that = (StackdriverMetricTimeseries) o;

        return getMetricType().equals(that.getMetricType()) &&
                getMetricLabels() == that.getMetricLabels();
    }
}

class Point {
    private final Number value;
    private final long timestamp;   // timestamp in milliseconds

    Point(long timestamp, Number value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    Number getValue() {
        return value;
    }

    long getTimestamp() {
        return timestamp;
    }
}

/**
 * Custom serializer for sending time series to the Stackdriver API.
 */
class StackdriverMetricTimeseriesSerializer extends StdSerializer<StackdriverMetricTimeseries> {
    private static final String CUSTOM_METRIC_DOMAIN = "custom.googleapis.com/druid/";

    public StackdriverMetricTimeseriesSerializer() {
        this(null);
    }

    private StackdriverMetricTimeseriesSerializer(Class<StackdriverMetricTimeseries> t) {
        super(t);
    }

    @Override
    public void serialize(
            StackdriverMetricTimeseries value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {
        Format format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        jgen.writeStartObject();

        jgen.writeObjectFieldStart("metric");
        jgen.writeStringField("type", CUSTOM_METRIC_DOMAIN + value.getMetricType());
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

        jgen.writeArrayFieldStart("points");

        for (Point point : value.getPoints()) {
            Date date = new Date(point.getTimestamp());
            String formattedTime = format.format(date);

            jgen.writeStartObject();

            jgen.writeObjectFieldStart("interval");
            jgen.writeStringField("endTime", formattedTime);
            jgen.writeStringField("startTime", formattedTime);
            jgen.writeEndObject();

            jgen.writeObjectFieldStart("value");
            jgen.writeStringField("int64Value", point.getValue().toString());
            jgen.writeEndObject();

            jgen.writeEndObject();
        }

        jgen.writeEndArray();

        jgen.writeEndObject();
    }
}
