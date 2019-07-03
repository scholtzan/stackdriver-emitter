package org.apache.druid.emitter.stackdriver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.druid.java.util.common.logger.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * Sends converted events to the Stackdriver API.
 */
class StackdriverSender {
    private static final Logger log = new Logger(StackdriverSender.class);
    private final ScheduledExecutorService scheduler;
    private final long consumeDelay;
    private final BlockingQueue<StackdriverMetricTimeseries> eventQueue;
    private final int flushThreshold;
    private final EventConsumer consumer;
    private final String projectId;

    StackdriverSender(
            int flushThreshold,
            int maxQueueSize,
            long consumeDelay,
            String projectId) {
        this.flushThreshold = flushThreshold;
        this.consumeDelay = consumeDelay;
        this.eventQueue = new ArrayBlockingQueue<>(maxQueueSize);
        this.scheduler = Executors.newScheduledThreadPool(2, new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("Stackdriver-%s")
                .build());
        this.consumer = new EventConsumer();
        this.projectId = projectId;
    }

    void start() {
        scheduler.scheduleAtFixedRate(
                consumer,
                consumeDelay,
                consumeDelay,
                TimeUnit.MILLISECONDS
        );
    }

    void enqueue(StackdriverMetricTimeseries event) {
        if (!eventQueue.offer(event)) {
            log.error("Error enqueueing event: " + event.getMetricType());
        }
    }

    void flush() {
        consumer.sendEvents();
    }

    void close() {
        flush();
        scheduler.shutdown();
    }

    private class EventConsumer implements Runnable {
        private final List<StackdriverMetricTimeseries> events;
        // keeps track of how many emitted events are going to be sent to Stackdriver
        // separate counter because some events get merged together in `events` into a time series (so .size() doesn't work)
        private int eventCount = 0;

        EventConsumer() {
            events = new ArrayList<>();
        }

        @Override
        public void run() {
            while (!eventQueue.isEmpty() && !scheduler.isShutdown()) {
                if (eventCount > flushThreshold) {
                    sendEvents();
                }

                final StackdriverMetricTimeseries metric = eventQueue.poll();

                // see: https://cloud.google.com/monitoring/api/ref_v3/rest/
                // events with the same metric descriptor _must_ be sent as single time series per request
                boolean existingMetric = false;
                for (int i = 0; i < events.size(); i++) {
                    if (metric != null &&
                        events.get(i).getMetricType().equals(metric.getMetricType()) &&
                        events.get(i).getMetricLabels().equals(metric.getMetricLabels())) {
                        StackdriverMetricTimeseries updatedEvent = events.get(i);
                        updatedEvent.addPoints(metric.getPoints());
                        events.set(i, updatedEvent);
                        existingMetric = true;
                        break;
                    }
                }

                if (!existingMetric) {
                    events.add(metric);
                }

                eventCount += 1;
            }
        }

        void sendEvents() {
            if (!events.isEmpty()) {
                HttpClient httpclient = HttpClients.createDefault();
                String url = "https://monitoring.googleapis.com/v3/projects/" + projectId + "/timeSeries";
                HttpPost request = new HttpPost(url);

                StackdriverTimeseriesPayload payload = new StackdriverTimeseriesPayload(events);
                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

                try {
                    // get credentials stored in environment variable for authenticating to Stackdriver API
                    String credentialsFile = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
                    File initialFile = new File(credentialsFile);
                    InputStream targetStream = new FileInputStream(initialFile);
                    GoogleCredentials cred = GoogleCredentials.fromStream(targetStream).createScoped(
                            Collections.singletonList("https://www.googleapis.com/auth/monitoring")
                    );
                    String token = cred.refreshAccessToken().getTokenValue();
                    request.addHeader("Authorization", "Bearer " + token);
                    try {
                        // create event JSON payload
                        String jsonPayload = ow.writeValueAsString(payload);
                        request.setEntity(new StringEntity(jsonPayload));
                        request.addHeader("Content-type", "application/json");

                        try {
                            httpclient.execute(request);
                        } catch (IOException ex) {
                            log.info(ex, "Failed to post events to Stackdriver.");
                        } finally {
                            request.releaseConnection();
                        }

                        events.clear();
                        eventCount = 0;
                    } catch (Exception e) {
                        log.error("Could not serialize payload: " + e.toString());
                    }
                }
                catch (Exception e) {
                    log.error("Error retrieving Google credentials: " + e);
                }
            }
        }

        private class StackdriverTimeseriesPayload {
            private List<StackdriverMetricTimeseries> timeSeries;

            StackdriverTimeseriesPayload(List<StackdriverMetricTimeseries> events) {
                this.timeSeries = events;
            }

            public List<StackdriverMetricTimeseries> getTimeSeries() {
                return timeSeries;
            }
        }
    }
}
