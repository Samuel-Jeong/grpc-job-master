package com.telcoware.job_master_app_demo.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * packageName    : com.telcoware.job_master_app_demo.config
 * fileName       : SqsJobConfig
 * author         : samuel
 * date           : 25. 10. 21.
 * description    : Schedule 설정
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 10. 21.        samuel       최초 생성
 */
@Getter
@Configuration
public class ScheduleConfig {

    @Value("${schedule.target-collector.thread.pool-size}")
    private Integer scheduleTargetCollectorThreadPoolSize;

    @Value("${schedule.target-collector.thread.queue-size}")
    private Integer scheduleTargetCollectorThreadPoolQueueSize;

    @Value("${schedule.job-allocator.thread.pool-size}")
    private Integer scheduleJobAllocatorThreadPoolSize;

    @Value("${schedule.job-allocator.thread.queue-size}")
    private Integer scheduleJobAllocatorThreadPoolQueueSize;

}
