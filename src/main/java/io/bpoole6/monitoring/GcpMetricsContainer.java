package io.bpoole6.monitoring;

import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class GcpMetricsContainer {
    private String apiBase;
    private List<Config> metrics;

    public List<String> generateAllMetricEndpoints(){
        return metrics.stream().flatMap((i)-> i.generateAllApiEndpoints().stream()).collect(Collectors.toList());
    }
}
