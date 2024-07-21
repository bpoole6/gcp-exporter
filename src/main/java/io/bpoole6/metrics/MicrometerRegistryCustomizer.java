package io.bpoole6.metrics;

import io.bpoole6.monitoring.GcpMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class MicrometerRegistryCustomizer implements
        MeterRegistryCustomizer<MeterRegistry> {
    private final GcpMetrics gcpMetrics;

    public MicrometerRegistryCustomizer(GcpMetrics gcpMetrics) {
        this.gcpMetrics = gcpMetrics;
    }

    @Override
    public void customize(MeterRegistry registry) {
        String[] arr = gcpMetrics.getGcpMetricsContainer().generateAllMetricEndpoints().toArray(String[]::new);
        registry.config().meterFilter(
                MeterFilter.deny(
                        (id) -> !StringUtils.equalsAny(id.getName(), arr))
        );
    }
}
