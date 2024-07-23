package io.bpoole6.scheduling;

import io.bpoole6.monitoring.GcpMetrics;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class ThreadPoolTaskSchedulerConfig {
    private GcpMetrics gcpMetrics;

    public ThreadPoolTaskSchedulerConfig(ConfigurableApplicationContext applicationContext,
                                         GcpMetrics gcpMetrics) {
        this.gcpMetrics = gcpMetrics;
    }

    @Bean("taskScheduler")
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler
                = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(gcpMetrics.getGcpMetricsContainer().getMetrics().size());
        threadPoolTaskScheduler.setPoolSize(1);
        threadPoolTaskScheduler.setThreadNamePrefix(
                "ThreadPoolTaskScheduler");
        return threadPoolTaskScheduler;
    }
}