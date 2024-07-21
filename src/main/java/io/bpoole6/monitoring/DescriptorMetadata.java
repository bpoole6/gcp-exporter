package io.bpoole6.monitoring;

import com.google.api.MetricDescriptor.MetricKind;
import com.google.api.MetricDescriptor.ValueType;
import lombok.Data;

@Data
public class DescriptorMetadata {
  private String type;
  private long samplePeriod;
  private long delayPeriod;
  private MetricKind metricKind;
  private ValueType valueType;
  private String description;
}
