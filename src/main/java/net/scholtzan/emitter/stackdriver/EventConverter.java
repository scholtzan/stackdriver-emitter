package net.scholtzan.emitter.stackdriver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Strings;
import org.apache.druid.java.util.common.ISE;
import org.apache.druid.java.util.common.logger.Logger;
import org.apache.druid.java.util.emitter.service.ServiceMetricEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EventConverter {
    private static final Logger log = new Logger(EventConverter.class);
    private final Map<String, Set<String>> metricMap;

    public EventConverter(ObjectMapper mapper, String metricMapPath) {
        metricMap = readMap(mapper, metricMapPath);
    }

    public StackdriverEvent convert(ServiceMetricEvent event) {
        String metric = event.getMetric();
        if (!metricMap.containsKey(metric)) {
            return null;
        }

        long timestamp = event.getCreatedTime().getMillis() / 1000L;
        Number value = event.getValue();

        HashMap<String, String> metricLabels = new HashMap<String, String>();

        // todo: do we need service and host?
        String service = event.getService();
        String host = event.getHost();
        metricLabels.put("service", service);
        metricLabels.put("host", host);

        Map<String, Object> userDims = event.getUserDims();
        for (String dim : metricMap.get(metric)) {
            if (userDims.containsKey(dim)) {
                Object dimValue = userDims.get(dim);
                metricLabels.put(dim, dimValue.toString());
            }
        }

        return new StackdriverEvent(metric, value, timestamp, metricLabels);
    }

    private Map<String, Set<String>> readMap(ObjectMapper mapper, String metricMapPath) {
        try {
            InputStream is;
            if (Strings.isNullOrEmpty(metricMapPath)) {
                log.info("Using default metric map");
                is = this.getClass().getClassLoader().getResourceAsStream("defaultMetrics.json");
            } else {
                log.info("Using default metric map located at [%s]", metricMapPath);
                is = new FileInputStream(new File(metricMapPath));
            }
            return mapper.readerFor(new TypeReference<Map<String, Set<String>>>() {
            }).readValue(is);
        } catch (IOException e) {
            throw new ISE(e, "Failed to parse metrics and dimensions");
        }
    }
}
