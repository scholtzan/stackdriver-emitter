package net.scholtzan.emitter.stackdriver;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import org.apache.druid.java.util.common.logger.Logger;
import org.apache.druid.java.util.emitter.core.Emitter;
import org.apache.druid.java.util.emitter.core.Event;
import org.apache.druid.java.util.emitter.service.ServiceMetricEvent;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class StackdriverEmitter implements Emitter {

    private static final Logger log = new Logger(StackdriverEmitter.class);
    private static final char DRUID_METRIC_SEPARATOR = '/';

    private final StackdriverSender sender;
    private final EventConverter converter;
    private final AtomicBoolean started = new AtomicBoolean(false);

    public StackdriverEmitter(MetricServiceClient client, StackdriverEmitterConfig config, ObjectMapper mapper) throws IOException {
        this.sender = new StackdriverSender(
                config.getHost(),
                config.getPort(),
                config.getFlushThreshold(),
                config.getMaxQueueSize(),
                config.getConsumeDelay(),
                config.getProjectId()
        );
        this.converter = new EventConverter(mapper, config.getMetricMapPath());
    }

    static StackdriverEmitter of(StackdriverEmitterConfig config, ObjectMapper mapper) throws IOException {
        MetricServiceClient client = MetricServiceClient.create();
        return new StackdriverEmitter(client, config, mapper);
    }

    @Override
    public void start() {
        synchronized (started) {
            if (!started.get()) {
                log.info("Start Stackdriver emitter.");
                sender.start();
                started.set(true);
            }
        }
    }

    @Override
    public void emit(Event event) {
        if (!started.get()) {
            log.error("emit() called before service was started.");
        } else {
            if (event instanceof ServiceMetricEvent) {
                StackdriverEvent metricEvent = converter.convert((ServiceMetricEvent) event);

                if (metricEvent != null) {
                    sender.enqueue(metricEvent);
                } else {
                    log.info("Event not configured to be emitted to Stackdriver: " + ((ServiceMetricEvent) event).getMetric());
                }
            }
        }
    }

    @Override
    public void flush() {
        sender.flush();
    }

    @Override
    public void close() {
        sender.close();
    }
}
