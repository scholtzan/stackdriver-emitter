package org.apache.druid.emitter.stackdriver;

import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.TestCase;
import org.apache.druid.java.util.common.DateTimes;
import org.apache.druid.java.util.emitter.service.ServiceMetricEvent;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class EventConverterTest extends TestCase {
    private EventConverter converter;

    @BeforeEach
    public void setUp() {
        converter = new EventConverter(new ObjectMapper(), null);
    }

    @Test
    public void testConvert() {
        DateTime dateTime = DateTimes.nowUtc();
        ServiceMetricEvent configuredEvent = new ServiceMetricEvent.Builder()
                .setDimension("dataSource", "foo:bar")
                .setDimension("type", "groupBy")
                .build(dateTime, "query/time", 10)
                .build("druid:broker", "127.0.0.1:8080");

        Map<String, Object> expectedTags = new HashMap<String, Object>();
        expectedTags.put("service", "druid:broker");
        expectedTags.put("host", "127.0.0.1:8080");
        expectedTags.put("dataSource", "foo:bar");
        expectedTags.put("type", "groupBy");

        StackdriverEvent stackdriverEvent = converter.convert(configuredEvent);
        assertEquals("query/time", stackdriverEvent.getEventPath());
        assertEquals(dateTime.getMillis() / 1000L, stackdriverEvent.getTimestamp());
        assertEquals(10, stackdriverEvent.getValue());
        assertEquals(expectedTags, stackdriverEvent.getMetricLabels());

        ServiceMetricEvent notConfiguredEvent = new ServiceMetricEvent.Builder()
                .setDimension("dataSource", "data-source")
                .setDimension("type", "groupBy")
                .build(dateTime, "foo/bar", 10)
                .build("broker", "brokerHost1");
        assertNull(converter.convert(notConfiguredEvent));
    }
}