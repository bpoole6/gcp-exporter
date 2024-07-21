package io.bpoole6.monitoring;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;

@Data
@Component
public class GcpMetrics {

    private GcpMetricsContainer gcpMetricsContainer;
    public GcpMetrics() throws IOException {
        String yml = new String(Files.readAllBytes(ResourceUtils.getFile("classpath:gcp-metrics.yml").toPath()));
        Yaml yaml  = new Yaml();
        GcpMetricsContainer item = yaml.loadAs(yml, GcpMetricsContainer.class);
        this.gcpMetricsContainer = item;
    }

}