package com.dovaj.job_master_app_demo.scheduler.schedule.unit;

import com.dovaj.job_master_app_demo.scheduler.job.Job;
import com.dovaj.job_master_app_demo.scheduler.schedule.handler.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.support.CronExpression;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * packageName    : com.dovaj.job_master_app_demo.scheduler.schedule.unit
 * fileName       : JobAdder
 * author         : samuel
 * date           : 24. 8. 5.
 * description    : 작업 예약 워커 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 24. 8. 5.        samuel       최초 생성
 */
public class JobAdder implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(JobAdder.class);

    private final JobScheduler jobScheduler;
    private final Job job;
    private final int executorIndex;
    // Prevent duplicate scheduling at the same cron instant
    private volatile long nextScheduledAtMs = -1L;
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);

    public JobAdder(JobScheduler jobScheduler, Job job, int executorIndex) {
        this.jobScheduler = jobScheduler;
        this.job = job;
        this.executorIndex = executorIndex;
    }

    @Override
    public void run() {
        if (job.isCron()) {
            scheduleNextCronFire();
            return;
        }

        scheduledThreadPoolExecutor.scheduleAtFixedRate(
                !job.isLasted() ?
                        (() -> {
                            if (isJobFinished(job)) {
                                jobScheduler.cancel(job);
                            } else {
                                job.decCurRemainRunCount();
                                jobScheduler.addJobToExecutor(executorIndex, job);
                            }
                        })
                        :
                        (() -> {
                            if (isJobFinished(job)) {
                                jobScheduler.cancel(job);
                            } else {
                                jobScheduler.addJobToExecutor(executorIndex, job);
                            }
                        }),
                job.getInitialDelay(), job.getInterval(), job.getTimeUnit()
        );
    }

    private void scheduleNextCronFire() {
        if (job.getIsFinished()) {
            return;
        }
        String exprStr = sanitizeCronExpression(job.getCronExpression());
        CronExpression expr = CronExpression.parse(exprStr);
        TimeZone tz = job.getTimeZone();
        ZoneId zone = (tz != null ? tz.toZoneId() : ZoneId.systemDefault());

        // Use a monotonic baseline to avoid clock-skew or boundary duplicates
        long nowMs = System.currentTimeMillis();
        long baselineMs = Math.max(nowMs, nextScheduledAtMs + 1);
        ZonedDateTime base = ZonedDateTime.ofInstant(Instant.ofEpochMilli(baselineMs), zone);

        ZonedDateTime next = expr.next(base);
        if (next == null) {
            jobScheduler.cancel(job);
            return;
        }
        long fireAtMs = next.toInstant().toEpochMilli();
        if (nextScheduledAtMs == fireAtMs) {
            // Already scheduled for this instant
            return;
        }
        long delayMs = Math.max(0L, Duration.between(ZonedDateTime.ofInstant(Instant.ofEpochMilli(nowMs), zone), next).toMillis());

        Runnable task = () -> {
            boolean finished = false;
            try {
                if (isJobFinished(job)) {
                    finished = true;
                    jobScheduler.cancel(job);
                    return;
                }
                if (!job.isLasted()) {
                    int left = job.decCurRemainRunCount();
                    if (left < 0) {
                        finished = true;
                        jobScheduler.cancel(job);
                        return;
                    }
                }
                jobScheduler.addJobToExecutor(executorIndex, job);
            } catch (Throwable t) {
                // swallow to keep scheduling chain alive
            } finally {
                if (!finished) {
                    // Schedule the next occurrence regardless of intermediate failures
                    scheduleNextCronFire();
                }
            }
        };

        try {
            scheduledThreadPoolExecutor.schedule(task, delayMs, TimeUnit.MILLISECONDS);
            nextScheduledAtMs = fireAtMs; // mark only after successful scheduling
        } catch (RejectedExecutionException rex) {
            // Recreate executor and retry once
            try {
                scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
                scheduledThreadPoolExecutor.schedule(task, delayMs, TimeUnit.MILLISECONDS);
                nextScheduledAtMs = fireAtMs;
            } catch (Exception ex) {
                // As a fallback, try again after 1s without advancing the marker
                try {
                    long fallbackDelay = Math.max(1000L, delayMs);
                    scheduledThreadPoolExecutor.schedule(this::scheduleNextCronFire, fallbackDelay, TimeUnit.MILLISECONDS);
                } catch (Exception ignore) {
                    // give up for now; next call chain may attempt again
                }
            }
        }
    }

    private String sanitizeCronExpression(String cron) {
        if (cron == null) return "0 * * * * *"; // default: every minute
        String trimmed = cron.trim();
        String[] parts = trimmed.split("\\s+");
        if (parts.length == 5) {
            // UNIX 5-field: add seconds field at start
            return "0 " + trimmed;
        }
        if (parts.length == 7) {
            // QUARTZ 7-field (with year): drop the year
            return String.join(" ", parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
        }
        // Assume 6-field already
        return trimmed;
    }

    public void stop() {
        scheduledThreadPoolExecutor.shutdown();
    }

    public boolean isJobFinished(Job job) {
        if (job == null) {
            return true;
        }

        return job.getIsFinished() || (!job.isLasted() && job.getCurRemainRunCount() <= 0);
    }

}
