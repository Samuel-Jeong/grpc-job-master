package com.telcoware.job_master_app_demo;

import com.telcoware.job_master_app_demo.config.MonitoringProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(MonitoringProperties.class)
public class JobMasterAppDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobMasterAppDemoApplication.class, args);
    }

}
