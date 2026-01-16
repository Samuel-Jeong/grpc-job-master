package com.telcoware.job_master_app_demo.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * packageName    : com.telcoware.job_master_app_demo.config
 * fileName       : SqsJobConfig
 * author         : samuel
 * date           : 25. 10. 21.
 * description    : JOB 설정
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 10. 21.        samuel       최초 생성
 */
@Getter
@Configuration
public class JobConfig {

    @Value("${job.master.thread-pool.core-size}")
    private Integer jobMasterCorePoolSize;

    @Value("${job.master.thread-pool.max-size}")
    private Integer jobMasterMaxPoolSize;

    @Value("${job.master.thread-pool.queue-capacity}")
    private Integer jobMasterQueueCapacity;

    @Value("${job.master.thread-pool.watermark}")
    private Integer jobMasterWatermark;

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        return new ThreadPoolTaskExecutorBuilder()
                .maxPoolSize(jobMasterMaxPoolSize)
                .corePoolSize(jobMasterCorePoolSize)
                .queueCapacity(jobMasterQueueCapacity)
                .threadNamePrefix("MASTER-Runner-")
                .build();
    }

}
