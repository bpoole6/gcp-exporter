package io.bpoole6.monitoring;

import lombok.Data;

import java.util.List;

@Data
public class Config {
    private String resourceDomain;
    private List<String> metrics;

    public String generateApiEndpoint(String metric) {
        return "%s.googleapis.com/%s".formatted(resourceDomain, metric);
    }
    public List<String> generateAllApiEndpoints(){
        return this.metrics.stream().map(this::generateApiEndpoint).toList();
    }
}
