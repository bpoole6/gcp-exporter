package io.bpoole6;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GcpMonitoringApplication {

  public static void main(String[] args) {
    SpringApplication.run(GcpMonitoringApplication.class, args);
  }

}
