package net.scholtzan.emitter.stackdriver;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import org.apache.druid.java.util.emitter.core.Emitter;
import org.apache.druid.java.util.emitter.core.Event;
import org.apache.druid.java.util.common.logger.Logger;
import org.apache.druid.java.util.emitter.service.ServiceMetricEvent;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

public class StackdriverEmitter implements Emitter {

    private static final Logger log = new Logger(StackdriverEmitter.class);
    private static final char DRUID_METRIC_SEPARATOR = '/';

    private final MetricServiceClient stackdriverClient;
    private final StackdriverEmitterConfig config;

    public StackdriverEmitter(MetricServiceClient client, StackdriverEmitterConfig config) {
        this.stackdriverClient = client;
        this.config = config;
    }

    static StackdriverEmitter of(StackdriverEmitterConfig config, ObjectMapper mapper) throws IOException
    {
        MetricServiceClient client = MetricServiceClient.create();

//        NonBlockingStackdriverClient client = new NonBlockingStackdriverClient(
//            config.getPrefix(),
//            config.getHostname(),
//            config.getPort(),
//            new StackdriverClientErrorHandler()
//            {
//                private int exceptionCount = 0;
//
//                @Override
//                public void handle(Exception exception)
//                {
//                    if (exceptionCount % 1000 == 0) {
//                        log.error(exception, "Error sending metric to Stackdriver.");
//                    }
//                    exceptionCount += 1;
//                }
//            }
//        );
//        return new StackdriverEmitter(config, mapper, client);

        return new StackdriverEmitter(client, config);
    }

    @Override
    public void start() { }

    @Override
    public void emit(Event event) {
        if (event instanceof ServiceMetricEvent) {
            ServiceMetricEvent metricEvent = (ServiceMetricEvent) event;
            String host = metricEvent.getHost();
            String service = metricEvent.getService();
            String metric = metricEvent.getMetric();
            Map<String, Object> userDims = metricEvent.getUserDims();
            Number value = metricEvent.getValue();
        }
    }

    @Override
    public void flush() { }

    @Override
    public void close() { }
}
