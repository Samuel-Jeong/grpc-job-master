package com.dovaj.job_master_app_demo.job.handler;

import com.dovaj.job_master_app_demo.config.JobConfig;
import com.dovaj.job_master_app_demo.job.dto.inf.JobInfo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * packageName    : com.dovaj.job_master_app_demo.job.handler
 * fileName       : JobMaster
 * author         : samuel
 * date           : 25. 10. 21.
 * description    : JOB 마스터
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 10. 21.        samuel       최초 생성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobMaster {

    private final JobConfig jobConfig;
    private ThreadPoolTaskExecutor executor;

    @PostConstruct
    public void init() {
        executor = jobConfig.threadPoolTaskExecutor();
    }

    // Thread-pool 에 job 할당
    public <T> void assignJob(JobInfo<T> jobInfo) {
        executor.execute(new JobWorker<>(jobInfo));
    }

    public boolean isActive() {
        int activeCount = executor.getActiveCount();
        int maxPoolSize = executor.getMaxPoolSize();
        int watermark = jobConfig.getJobMasterWatermark();
        return (((double) activeCount / (double) maxPoolSize)) * 100 <= watermark;
    }

}
