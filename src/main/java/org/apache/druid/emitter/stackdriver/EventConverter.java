package org.apache.druid.emitter.stackdriver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

/**
 * Converts emitted druid events to Stackdriver time series.
 */
class EventConverter {
    private static final Logger log = new Logger(EventConverter.class);
    private final Map<String, Set<String>> metricMap;

    EventConverter(ObjectMapper mapper, String metricMapPath) {
        metricMap = readMap(mapper, metricMapPath);
    }

    StackdriverMetricTimeseries convert(ServiceMetricEvent event) {
        String metric = event.getMetric();

        long timestamp = event.getCreatedTime().getMillis();
        Number value = event.getValue();

        HashMap<String, String> metricLabels = new HashMap<>();

        String service = event.getService();
        String host = event.getHost();
        metricLabels.put("service", service);
        metricLabels.put("host", host);

        Map<String, Object> userDims = event.getUserDims();

        // if the metric map is empty then all emitted events will be sent to Stackdriver
        if (metricMap.isEmpty()) {
            for (Map.Entry<String, Object> entry : userDims.entrySet()) {
                metricLabels.put(entry.getKey(), entry.getValue().toString());
            }
        } else {
            for (String dim : metricMap.get(metric)) {
                if (userDims.containsKey(dim)) {
                    Object dimValue = userDims.get(dim);
                    metricLabels.put(dim, dimValue.toString());
                }
            }
        }

        return new StackdriverMetricTimeseries(metric, value, timestamp, metricLabels);
    }

    private Map<String, Set<String>> readMap(ObjectMapper mapper, String metricMapPath) {
        try {
            InputStream is;
            if (metricMapPath == null || metricMapPath.isEmpty()) {
                log.info("Use all metrics");
                return new HashMap<>();
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
