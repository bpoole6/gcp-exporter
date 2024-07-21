package io.bpoole6.scheduling;

import io.bpoole6.metrics.MetricsRegistry;
import io.bpoole6.monitoring.GcpExporter;
import io.bpoole6.monitoring.GcpMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ScheduledTasks {

    private final TaskScheduler executor;

    public ScheduledTasks(@Qualifier("taskScheduler") TaskScheduler taskExecutor,
                          GcpMetrics gcpMetrics, @Value("${spring.cloud.gcp.project-id}") String projectId, MetricsRegistry metricsRegistry) {
        this.executor = taskExecutor;

        gcpMetrics.getGcpMetricsContainer().getMetrics().forEach((i) -> {
            scheduling(() -> {
                try {
                    new GcpExporter(projectId, metricsRegistry, i).run();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            });
        });
    }

    public void scheduling(final Runnable task) {
        executor.scheduleWithFixedDelay(task, 60000);
    }
}