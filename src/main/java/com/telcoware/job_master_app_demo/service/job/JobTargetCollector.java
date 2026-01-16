package com.telcoware.job_master_app_demo.service.job;

import com.telcoware.job_master_app_demo.config.ScheduleConfig;
import com.telcoware.job_master_app_demo.job.dto.impl.JobTargetSelectionWork;
import com.telcoware.job_master_app_demo.scheduler.job.Job;
import com.telcoware.job_master_app_demo.scheduler.job.JobBuilder;
import com.telcoware.job_master_app_demo.scheduler.schedule.ScheduleManager;
import com.telcoware.job_master_app_demo.service.aws.elasticache.AwsValKeyService;
import com.telcoware.job_master_app_demo.service.grpc.GrpcClientService;
import com.telcoware.job_master_app_demo.util.GsonUtil;
import com.telcoware.job_master_app_demo.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * packageName    : com.telcoware.job_master_app_demo.service.job
 * fileName       : JobTargetCollector
 * author         : samuel
 * date           : 25. 10. 21.
 * description    : 작업 대상 조회자
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 10. 21.        samuel       최초 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobTargetCollector implements ApplicationListener<ContextRefreshedEvent> {

    private ScheduleManager scheduleManager;
    private boolean isSchedulerEnabled = false;
    private String scheduleKey;

    private final ScheduleConfig scheduleConfig;

    private final GrpcClientService grpcClientService;
    private final AwsValKeyService awsValKeyService;
    private final GsonUtil gsonUtil;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // ScheduleManager init
        scheduleManager = new ScheduleManager();
        scheduleKey = "JOB_TARGET_COLLECTOR_SCHEDULE_KEY:" + UUID.randomUUID();
        isSchedulerEnabled = scheduleManager.initJob(
                scheduleKey,
                scheduleConfig.getScheduleTargetCollectorThreadPoolSize(),
                scheduleConfig.getScheduleTargetCollectorThreadPoolQueueSize()
        );
        if (isSchedulerEnabled) {
            log.info("Success to init job scheduler. ({})", scheduleKey);
        }

        assignJobTargetSelectionWork();
    }

    private void assignJobTargetSelectionWork() {
        String className = JobTargetSelectionWork.class.getSimpleName();
        if (!isSchedulerEnabled) {
            log.warn("Fail to start [{}]. Scheduler is not started.", className);
            return;
        }

        Job job = new JobBuilder()
                .setScheduleManager(scheduleManager)
                .setName(className + ":" + UUID.randomUUID())
                .setInitialDelay(0)
                .setInterval(5)
                .setTimeUnit(TimeUtil.convertStringToTimeUnit("s"))
                .setPriority(1)
                .setTotalRunCount(0)
                .setIsLasted(true)
                .setJobFinishCallBack(() -> log.info("[{}] : removedJob", className))
                .build();
        JobTargetSelectionWork jobTargetSelectionWork = new JobTargetSelectionWork(
                job,
                grpcClientService,
                awsValKeyService,
                gsonUtil
        );
        jobTargetSelectionWork.start();
        if (scheduleManager.startJob(scheduleKey, jobTargetSelectionWork.getJob())) {
            log.info("Success to start [{}].", className);
        } else {
            log.warn("Fail to start [{}].", className);
        }
    }

}
