package org.apache.druid.emitter.stackdriver;

import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import junit.framework.TestCase;
import org.apache.druid.jackson.DefaultObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class StackdriverMetricTimeseriesTest extends TestCase {
    private ObjectMapper mapper = new DefaultObjectMapper();

    @BeforeEach
    public void setUp() {
        mapper.setInjectableValues(new InjectableValues.Std().addValue(ObjectMapper.class, new DefaultObjectMapper()));
    }

    @Test
    public void testAddingPoints() {
        HashMap<String, String> labels = new HashMap<>();
        StackdriverMetricTimeseries p1 = new StackdriverMetricTimeseries("test1", 23, 1000L, labels);
        StackdriverMetricTimeseries p2 = new StackdriverMetricTimeseries("test2", 54, 1000L, labels);
        assertEquals(p1.getPoints().size(), 1);
        p1.addPoints(p2.getPoints());
        assertEquals(p1.getPoints().size(), 2);
    }

    @Test
    public void testEventSerialization() throws Exception {
        HashMap<String, String> labels = new HashMap<>();
        labels.put("test", "ping");
        StackdriverMetricTimeseries event = new StackdriverMetricTimeseries("foo/bar", 42.0, 1562008168973L, labels);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        String jsonEvent = ow.writeValueAsString(event);
        String expectedJson = "{\n" +
                "  \"metric\" : {\n" +
                "    \"type\" : \"custom.googleapis.com/druid/foo/bar\",\n" +
                "    \"labels\" : {\n" +
                "      \"test\" : \"ping\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"resource\" : {\n" +
                "    \"type\" : \"global\"\n" +
                "  },\n" +
                "  \"metricKind\" : \"GAUGE\",\n" +
                "  \"valueType\" : \"INT64\",\n" +
                "  \"points\" : [ {\n" +
                "    \"interval\" : {\n" +
                "      \"endTime\" : \"2019-07-01T12:09:28.973Z\",\n" +
                "      \"startTime\" : \"2019-07-01T12:09:28.973Z\"\n" +
                "    },\n" +
                "    \"value\" : {\n" +
                "      \"int64Value\" : \"42\"\n" +
                "    }\n" +
                "  } ]\n" +
                "}";

        assertEquals(expectedJson, jsonEvent);
    }

    @Test
    public void testEventLargeValuesSerialization() throws Exception {
        HashMap<String, String> labels = new HashMap<>();
        labels.put("test", "ping");
        StackdriverMetricTimeseries event = new StackdriverMetricTimeseries("foo/bar", 1922337203965477580L, 1562008168973L, labels);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        String jsonEvent = ow.writeValueAsString(event);
        String expectedJson = "{\n" +
                "  \"metric\" : {\n" +
                "    \"type\" : \"custom.googleapis.com/druid/foo/bar\",\n" +
                "    \"labels\" : {\n" +
                "      \"test\" : \"ping\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"resource\" : {\n" +
                "    \"type\" : \"global\"\n" +
                "  },\n" +
                "  \"metricKind\" : \"GAUGE\",\n" +
                "  \"valueType\" : \"INT64\",\n" +
                "  \"points\" : [ {\n" +
                "    \"interval\" : {\n" +
                "      \"endTime\" : \"2019-07-01T12:09:28.973Z\",\n" +
                "      \"startTime\" : \"2019-07-01T12:09:28.973Z\"\n" +
                "    },\n" +
                "    \"value\" : {\n" +
                "      \"int64Value\" : \"1922337203965477580\"\n" +
                "    }\n" +
                "  } ]\n" +
                "}";

        assertEquals(expectedJson, jsonEvent);
    }
}
