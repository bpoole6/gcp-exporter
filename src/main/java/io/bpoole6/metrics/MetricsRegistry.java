package io.bpoole6.metrics;

import com.google.api.MetricDescriptor.ValueType;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import io.bpoole6.monitoring.DescriptorMetadata;
import io.bpoole6.monitoring.MetricDetail;
import io.bpoole6.monitoring.MetricKey;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Counter.Builder;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MetricsRegistry {
  private final Map<MetricKey, MetricDetail> guagesMap = new ConcurrentHashMap<>() {
  };
  private final Map<MetricKey, Counter> counterMap = new ConcurrentHashMap<>();
  private final Map<String, DescriptorMetadata> descriptorMetadataMap;
  private final String projectId;
  private final MeterRegistry registry;
  public MetricsRegistry(MeterRegistry registry, @Value("${spring.cloud.gcp.project-id}") String projectId) throws IOException {
    this.registry = registry;
    this.descriptorMetadataMap = collectDescriptorMetadata();
    this.projectId = projectId;
  }
  public Counter getOrCreateCounters(MetricDetail detail) {

    return this.counterMap.computeIfAbsent(detail.getMetricKey(), (m) -> {
      Builder counterBuilder = Counter.builder(detail.getMetricKey().getMetricType());
      m.getMapData().forEach(counterBuilder::tag);
      counterBuilder.description(detail.getDescriptorMetadata().getDescription());
      return counterBuilder.register(registry);
    });
  }

  public MetricDetail getOrCreateGauge(MetricDetail detail) {

    return this.guagesMap.computeIfAbsent(detail.getMetricKey(), (m) -> {
      Gauge.Builder gaugeBuilder = Gauge.builder(detail.getMetricKey().getMetricType(), detail,
          (d) -> getValue(d));
      gaugeBuilder.description(detail.getDescriptorMetadata().getDescription());
      m.getMapData().forEach(gaugeBuilder::tag);
      gaugeBuilder.register(registry);
      return detail;
    });
  }

  public  Map<String, DescriptorMetadata> collectDescriptorMetadata() throws IOException {
    Map<String, DescriptorMetadata> metricsTimeIntervalMap = new HashMap<>();
    try (MetricServiceClient metricServiceClient = MetricServiceClient.create()) {
      var item = metricServiceClient.listMetricDescriptors(
          "projects/"+ this.projectId);
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

  public double getValue(MetricDetail detail) {
    ValueType valueType = this.descriptorMetadataMap.get(detail.getMetricKey().getMetricType()).getValueType();
    return switch (valueType) {
      case INT64 -> detail.getValue().getInt64Value();
      case BOOL -> Boolean.TRUE.equals(detail.getValue().getBoolValue()) ? 1 : 0;
      case DOUBLE -> detail.getValue().getDoubleValue();
      default -> throw new RuntimeException("not implemented yet");
    };
  }
}
