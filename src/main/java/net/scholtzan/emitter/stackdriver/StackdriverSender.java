package net.scholtzan.emitter.stackdriver;

import com.google.api.Metric;
import com.google.api.MonitoredResource;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.monitoring.v3.*;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.Timestamps;
import org.apache.druid.java.util.common.logger.Logger;

import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class StackdriverSender {
    private static final Logger log = new Logger(StackdriverSender.class);
    private final ScheduledExecutorService scheduler;
    private final long consumeDelay;
    private final MetricServiceClient stackdriverClient;
    private final BlockingQueue<StackdriverEvent> eventQueue;
    private final int flushThreshold;
    private final EventConsumer consumer;
    private final ProjectName projectName;

    public StackdriverSender(
        String host,
        int port,
        int connectionTimeout,
        int readTimeout,
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
        this.stackdriverClient = MetricServiceClient.create();
        this.consumer = new EventConsumer();
        this.projectName = ProjectName.of(projectId);
    }

    public void start() {
        scheduler.scheduleWithFixedDelay(
            consumer,
            consumeDelay,
            consumeDelay,
            TimeUnit.MILLISECONDS
        );
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

                for (StackdriverEvent event: events) {
                    try {
                        TimeInterval interval = TimeInterval.newBuilder()
                            .setEndTime(Timestamps.fromMillis(event.getTimestamp()))
                            .build();

                        Point point = Point.newBuilder()
                                .setInterval(interval)
                                .setValue(TypedValue.parseFrom(event.getValue().getBytes()))
                                .build();

                        Map<String, String> metricLabels = new HashMap<String, String>();

                        Metric metric = Metric.newBuilder()
                                .setType("custom.googleapis.com/" + event.getEventPath())
                                .putAllLabels(metricLabels)
                                .build();

                        List<Point> pointList = new ArrayList<Point>();
                        pointList.add(point);

                        MonitoredResource resource = MonitoredResource.newBuilder()
                                .setType("druid")
                                .putAllLabels(new HashMap<String, String>())
                                .build();

                        TimeSeries timeSeries = TimeSeries.newBuilder()
                                .setMetric(metric)
                                .setResource(resource)
                                .addAllPoints(pointList)
                                .build();

                        List<TimeSeries> timeSeriesList = new ArrayList<TimeSeries>();
                        timeSeriesList.add(timeSeries);

                        CreateTimeSeriesRequest request = CreateTimeSeriesRequest.newBuilder()
                                .setName(projectName.toString())
                                .addAllTimeSeries(timeSeriesList)
                                .build();

                        stackdriverClient.createTimeSeries(request);
                    } catch (InvalidProtocolBufferException e) {
                        log.error("Error converting event value to Stackdriver type: " + e);
                    }
                }
            }
        }
    }
}