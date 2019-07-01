package org.apache.druid.emitter.stackdriver;

import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import junit.framework.TestCase;
import org.apache.druid.jackson.DefaultObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class StackdriverEventTest extends TestCase {
    private ObjectMapper mapper = new DefaultObjectMapper();

    @BeforeEach
    public void setUp() {
        mapper.setInjectableValues(new InjectableValues.Std().addValue(ObjectMapper.class, new DefaultObjectMapper()));
    }

    @Test
    public void testEventSerialization() throws Exception {
        HashMap<String, String> labels = new HashMap<>();
        labels.put("test", "ping");
        StackdriverEvent event = new StackdriverEvent("foo/bar", 42, 1562008168973L, labels);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String jsonEvent = ow.writeValueAsString(event);
        String expectedJson = "{\n" +
                "  \"metric\" : {\n" +
                "    \"type\" : \"foo/bar\",\n" +
                "    \"labels\" : {\n" +
                "      \"test\" : \"ping\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"resource\" : {\n" +
                "    \"type\" : \"global\"\n" +
                "  },\n" +
                "  \"metricKind\" : \"GAUGE\",\n" +
                "  \"valueType\" : \"INT64\",\n" +
                "  \"points\" : {\n" +
                "    \"interval\" : {\n" +
                "      \"endTime\" : \"2019-07-01T12:09:28.973Z\",\n" +
                "      \"startTime\" : \"2019-07-01T12:09:28.973Z\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        assertEquals(expectedJson, jsonEvent);
    }
}