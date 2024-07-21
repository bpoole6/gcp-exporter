package io.bpoole6.monitoring;

import com.google.api.MetricDescriptor.MetricKind;
import com.google.monitoring.v3.TypedValue;
import lombok.Data;

@Data
public class MetricDetail {
  private MetricKey metricKey;
  private TypedValue value;
  private DescriptorMetadata descriptorMetadata;

  public boolean isGauge(){
    return descriptorMetadata.getMetricKind() != MetricKind.CUMULATIVE;
  }
}
