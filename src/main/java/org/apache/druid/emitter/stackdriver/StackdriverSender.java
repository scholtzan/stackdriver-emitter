package org.apache.druid.emitter.stackdriver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.druid.java.util.common.logger.Logger;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class StackdriverSender {
    private static final String CUSTOM_METRIC_DOMAIN = "custom.googleapis.com/druid/";

    private static final Logger log = new Logger(StackdriverSender.class);
    private final ScheduledExecutorService scheduler;
    private final long consumeDelay;
    private final BlockingQueue<StackdriverEvent> eventQueue;
    private final int flushThreshold;
    private final EventConsumer consumer;
    private final String projectId;

    public StackdriverSender(
            int flushThreshold,
            int maxQueueSize,
            long consumeDelay,
            String projectId) throws IOException {
        this.flushThreshold = flushThreshold;
        this.consumeDelay = consumeDelay;
        this.eventQueue = new ArrayBlockingQueue<StackdriverEvent>(maxQueueSize);
        this.scheduler = Executors.newScheduledThreadPool(2, new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("Stackdriver-%s")
                .build());
        this.consumer = new EventConsumer();
        this.projectId = projectId;
    }

    public void start() {
        scheduler.scheduleWithFixedDelay(
                consumer,
                consumeDelay,
                consumeDelay,
                TimeUnit.MILLISECONDS
        );
    }

    public void enqueue(StackdriverEvent event) {
        log.error("Enqueue");
        if (!eventQueue.offer(event)) {
            log.error("Error enqueueing event: " + event);
        }
    }

    public void flush() {
        //todo: FLUSH_TIMEOUT?
        consumer.sendEvents();
    }

    public void close() {
        flush();
        scheduler.shutdown();
    }

    private class EventConsumer implements Runnable {
        private final List<StackdriverEvent> events;

        public EventConsumer() {
            events = new ArrayList<StackdriverEvent>(flushThreshold);
        }

        @Override
        public void run() {
            while (!eventQueue.isEmpty() && !scheduler.isShutdown()) {
                StackdriverEvent metric = eventQueue.poll();
                events.add(metric);

                if (events.size() > flushThreshold) {
                    sendEvents();
                }
            }
        }

        public void sendEvents() {
            if (!events.isEmpty()) {
                // todo: events of the same metric can be put into one time series
                HttpPost request = new HttpPost("https://monitoring.googleapis.com/v3/projects/" + projectId + "/timeSeries");
                StackdriverTimeseriesPayload payload = new StackdriverTimeseriesPayload(events);
                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                try {
                    String jsonPayload = ow.writeValueAsString(payload);
                    request.setEntity(new StringEntity(jsonPayload, ContentType.DEFAULT_TEXT));
                    request.addHeader("content-type", "application/x-www-form-urlencoded");
                } catch (Exception e) {
                    log.error("Could not serialize payload: " + e.toString());
                }
            }
        }

        private class StackdriverTimeseriesPayload {
            private List<StackdriverEvent> timeSeries;

            public StackdriverTimeseriesPayload(List<StackdriverEvent> events) {
                this.timeSeries = events;
            }
        }
    }
}