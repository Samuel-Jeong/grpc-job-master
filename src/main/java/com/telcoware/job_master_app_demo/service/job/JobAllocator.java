package com.telcoware.job_master_app_demo.service.job;

import com.telcoware.job_master_app_demo.config.CronJobConfig;
import com.telcoware.job_master_app_demo.config.ScheduleConfig;
import com.telcoware.job_master_app_demo.job.dto.impl.CronJobAllocationWork;
import com.telcoware.job_master_app_demo.scheduler.job.Job;
import com.telcoware.job_master_app_demo.scheduler.job.JobBuilder;
import com.telcoware.job_master_app_demo.scheduler.schedule.ScheduleManager;
import com.telcoware.job_master_app_demo.service.aws.elasticache.AwsValKeyService;
import com.telcoware.job_master_app_demo.service.grpc.GrpcClientService;
import com.telcoware.job_master_app_demo.util.GsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

/**
 * packageName    : com.telcoware.job_master_app_demo.service.job
 * fileName       : JobAllocator
 * author         : samuel
 * date           : 25. 10. 21.
 * description    : 작업 할당자
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 10. 21.        samuel       최초 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobAllocator implements ApplicationListener<ContextRefreshedEvent> {

    private final CronJobConfig cronjobConfig;

    private ScheduleManager scheduleManager;
    private boolean isSchedulerEnabled = false;
    private String scheduleKey;

    private final ScheduleConfig scheduleConfig;

    private final GrpcClientService grpcClientService;

    private final JobReporter jobReporter;

    private final AwsValKeyService awsValKeyService;

    private final GsonUtil gsonUtil;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // ScheduleManager init
        scheduleManager = new ScheduleManager();
        scheduleKey = "JOB_ALLOCATOR_SCHEDULE_KEY:" + UUID.randomUUID();
        isSchedulerEnabled = scheduleManager.initJob(
                scheduleKey,
                scheduleConfig.getScheduleJobAllocatorThreadPoolSize(),
                scheduleConfig.getScheduleJobAllocatorThreadPoolQueueSize()
        );
        if (isSchedulerEnabled) {
            log.info("Success to init job scheduler. ({})", scheduleKey);
        }

        assignCronJobAllocationWork();
    }

    private void assignCronJobAllocationWork() {
        String className = CronJobAllocationWork.class.getSimpleName();
        if (!isSchedulerEnabled) {
            log.warn("Fail to start [{}]. Scheduler is not started.", className);
            return;
        }

        for (Map.Entry<String, LinkedHashMap<String, String>> entry : cronjobConfig.getAllCronJobs()) {
            String cronJobName = entry.getKey();
            LinkedHashMap<String, String> cronInfoMap = entry.getValue();
            String cronExpression = cronInfoMap.get("cron");
            String timezone = cronInfoMap.get("timezone");

            String tzId = (timezone == null || timezone.isBlank()) ? TimeZone.getDefault().getID() : timezone;
            Job job = new JobBuilder()
                    .setScheduleManager(scheduleManager)
                    .setName(className + "-" + cronJobName)
                    .setInitialDelay(0)
                    .useCron(cronExpression, TimeZone.getTimeZone(tzId))
                    .setPriority(1)
                    .setTotalRunCount(0)
                    .setIsLasted(true)
                    .setJobFinishCallBack(() -> log.info("[{}-{}] : removedJob", className, cronJobName))
                    .build();
            CronJobAllocationWork cronJobAllocationWork = new CronJobAllocationWork(
                    job,
                    cronJobName,
                    grpcClientService,
                    jobReporter,
                    awsValKeyService,
                    gsonUtil
            );
            cronJobAllocationWork.start();
            if (scheduleManager.startJob(scheduleKey, cronJobAllocationWork.getJob())) {
                log.info("Success to start [{}-{}].", className, cronJobName);
            } else {
                log.warn("Fail to start [{}-{}].", className, cronJobName);
            }
        }
    }

}
