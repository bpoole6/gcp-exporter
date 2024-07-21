package io.bpoole6.monitoring;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class MetricKey {
  private final String metricType;
  private final Map<String, String> mapData;
  public MetricKey(String metricType, Map<String, String> mapData) {
    this.metricType = metricType;
    this.mapData = Collections.unmodifiableMap(new HashMap<>(mapData));
  }
}
