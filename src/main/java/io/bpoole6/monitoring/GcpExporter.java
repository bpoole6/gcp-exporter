package io.bpoole6.monitoring;

import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.cloud.monitoring.v3.MetricServiceClient.ListTimeSeriesPagedResponse;
import com.google.monitoring.v3.ListTimeSeriesRequest;
import com.google.monitoring.v3.ListTimeSeriesRequest.TimeSeriesView;
import com.google.monitoring.v3.TimeInterval;
import com.google.monitoring.v3.TimeSeries;
import com.google.protobuf.Timestamp;
import io.bpoole6.metrics.MetricsRegistry;
import io.micrometer.core.instrument.Counter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;

@Slf4j
public class GcpExporter {

    private final Map<String, DescriptorMetadata> timeIntervals;
    private final Config config;
    private final MetricsRegistry metricsRegistry;
    private String projectId;

    public GcpExporter(String projectId, MetricsRegistry metricsRegistry, Config config) throws IOException {
        this.projectId = projectId;
        this.timeIntervals = collectDescriptorMetadata();
        this.config = config;
        this.metricsRegistry = metricsRegistry;
    }


    public final void run() throws IOException {
        log.info("Running Monitoring for project:{} resource:{}", projectId, config.getResourceDomain());
        List<MetricDetail> details = new ArrayList<>();
        for (String filter : config.getMetrics()) {
            try {
                String endpoint = config.generateApiEndpoint(filter);
                ListTimeSeriesPagedResponse list = findListTimeSeries(projectId,
                        "metric.type = \"%s\"".formatted(endpoint), getTimeInterval(endpoint));
                for (TimeSeries ts : list.getPage().getResponse().getTimeSeriesList()) {
                    toMetricDetail(ts).ifPresent((m) -> {
                        details.add(m);
                        if (m.isGauge()) {
                            this.metricsRegistry.getOrCreateGauge(m).setValue(m.getValue());
                        } else {
                            Counter c = this.metricsRegistry.getOrCreateCounters(m);
                            c.increment(this.metricsRegistry.getValue(m) - c.count());
                        }
                    });

                }
            } catch (Exception e) {
                log.info("Failed to collect metrics for {}", filter, e);
            }
        }
    }

    public Map<String, DescriptorMetadata> collectDescriptorMetadata() throws IOException {
        Map<String, DescriptorMetadata> metricsTimeIntervalMap = new HashMap<>();
        try (MetricServiceClient metricServiceClient = MetricServiceClient.create()) {
            var item = metricServiceClient.listMetricDescriptors(
                    "projects/" + projectId);
            for (var i : item.getPage().getResponse().getMetricDescriptorsList()) {
                DescriptorMetadata interval = new DescriptorMetadata();
                interval.setType(i.getType());
                interval.setDelayPeriod(i.getMetadata().getIngestDelay().getSeconds());
                interval.setSamplePeriod(i.getMetadata().getSamplePeriod().getSeconds());
                interval.setMetricKind(i.getMetricKind());
                interval.setValueType(i.getValueType());
                interval.setDescription(i.getDescription());
                metricsTimeIntervalMap.put(i.getType(), interval);
            }
        }
        return metricsTimeIntervalMap;
    }


    public TimeInterval getTimeInterval(String endpoint) {
        long delay = this.timeIntervals.get(endpoint).getDelayPeriod();
        Timestamp startTime = Timestamp.newBuilder()
                .setSeconds(System.currentTimeMillis() / 1000 - delay).build();
        Timestamp endTime = Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000)
                .build();
        return TimeInterval.newBuilder().setStartTime(startTime).setEndTime(endTime).build();
    }


    private Optional<MetricDetail> toMetricDetail(TimeSeries timeSeries) {
        MetricDetail metricDetail = new MetricDetail();
        DescriptorMetadata descriptorMetadata = timeIntervals.get(timeSeries.getMetric().getType());
        if (timeSeries.getPointsList().isEmpty()) {
            return Optional.empty();
        }
        metricDetail.setDescriptorMetadata(descriptorMetadata);
        metricDetail.setValue(timeSeries.getPointsList().get(0).getValue());
        MetricKey key = new MetricKey(timeSeries.getMetric().getType(),
                timeSeries.getResource().getLabelsMap());
        metricDetail.setMetricKey(key);
        return Optional.of(metricDetail);
    }

    private ListTimeSeriesPagedResponse findListTimeSeries(String projectId, String filter,
                                                           TimeInterval interval)
            throws IOException {
        try (MetricServiceClient metricServiceClient = MetricServiceClient.create()) {
            return metricServiceClient.listTimeSeries(
                    ListTimeSeriesRequest.newBuilder()
                            .setInterval(interval)
                            .setFilter(filter)
                            .setView(TimeSeriesView.FULL)
                            .setName("projects/" + projectId).build());
        }
    }
}
