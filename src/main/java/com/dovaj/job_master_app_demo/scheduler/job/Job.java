package com.dovaj.job_master_app_demo.scheduler.job;

import com.dovaj.job_master_app_demo.scheduler.schedule.ScheduleManager;
import com.dovaj.job_master_app_demo.scheduler.schedule.handler.callback.JobFinishCallBack;

import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * packageName    : com.capshome.iotgw.scheduler.job
 * fileName       : Job
 * author         : samuel
 * date           : 24. 8. 5.
 * description    : ScheduleManager 에서 할당하는 작업 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 24. 8. 5.        samuel       최초 생성
 */
public class Job {

    private final AtomicInteger curRemainRunCount = new AtomicInteger(0);
    private final AtomicBoolean isFinished = new AtomicBoolean(false);

    /** 스케줄 방식 (기본: 고정 주기) */
    private JOB_SCHEDULE_MODE scheduleMode = JOB_SCHEDULE_MODE.FIXED_RATE;

    /** 크론 기반일 때 사용: 크론 표현식 (UNIX 5필드 또는 QUARTZ 6/7필드) */
    private String cronExpression = null;

    private ScheduleManager scheduleManager = null;
    private String name = null;
    private int initialDelay = 0;
    private int interval = 0;
    private TimeUnit timeUnit = null; // ex) TimeUnit.MILLISECONDS
    private int priority = 0;
    private int totalRunCount = 0;
    private boolean isLasted = false;
    private String scheduleUnitKey = null;
    private Runnable runnable = null;
    private TimeZone timeZone = null;

    private JobFinishCallBack jobFinishCallBack = null;

    public Job() {
        // Nothing
    }

    public ScheduleManager getScheduleManager() {
        return scheduleManager;
    }

    public void setScheduleManager(ScheduleManager scheduleManager) {
        this.scheduleManager = scheduleManager;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getInitialDelay() {
        return initialDelay;
    }

    public void setInitialDelay(int initialDelay) {
        this.initialDelay = initialDelay;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    /** 스케줄 방식 확인 */
    public JOB_SCHEDULE_MODE getScheduleMode() {
        return scheduleMode;
    }

    public void setJOB_SCHEDULE_MODE(JOB_SCHEDULE_MODE scheduleMode) {
        this.scheduleMode = scheduleMode;
    }

    /** 크론 표현식 접근자 */
    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
        if (cronExpression != null) {
            this.scheduleMode = JOB_SCHEDULE_MODE.CRON;
        }
    }

    /** TimeZone 접근자 (크론 트리거 평가 시 사용) */
    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getTotalRunCount() {
        return totalRunCount;
    }

    public void setTotalRunCount(int totalRunCount) {
        this.totalRunCount = totalRunCount;
    }

    public int getCurRemainRunCount() {
        return curRemainRunCount.get();
    }

    public void setCurRemainRunCount(int curRemainRunCount) {
        this.curRemainRunCount.set(curRemainRunCount);
    }

    public int incCurRemainRunCount() {
        return curRemainRunCount.incrementAndGet();
    }

    public int decCurRemainRunCount() {
        return curRemainRunCount.decrementAndGet();
    }

    public boolean isLasted() {
        return isLasted;
    }

    public void setLasted(boolean lasted) {
        isLasted = lasted;
    }

    public boolean getIsFinished() {
        return isFinished.get();
    }

    public void setIsFinished(boolean isFinished) {
        this.isFinished.set(isFinished);
    }

    public String getScheduleUnitKey() {
        return scheduleUnitKey;
    }

    public void setScheduleUnitKey(String scheduleUnitKey) {
        this.scheduleUnitKey = scheduleUnitKey;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    public JobFinishCallBack getJobFinishCallBack() {
        return jobFinishCallBack;
    }

    public void setJobFinishCallBack(JobFinishCallBack jobFinishCallBack) {
        this.jobFinishCallBack = jobFinishCallBack;
    }

    /**
     * 현재 설정 유효성 점검.
     * - FIXED_RATE: interval>0, timeUnit!=null
     * - CRON: cronExpression not null/blank
     */
    public void validateConfig() {
        if (scheduleMode == JOB_SCHEDULE_MODE.FIXED_RATE) {
            if (interval <= 0) {
                throw new IllegalArgumentException("interval must be > 0 for FIXED_RATE job: " + name);
            }
            if (timeUnit == null) {
                throw new IllegalArgumentException("timeUnit must not be null for FIXED_RATE job: " + name);
            }
        } else if (scheduleMode == JOB_SCHEDULE_MODE.CRON) {
            if (cronExpression == null || cronExpression.trim().isEmpty()) {
                throw new IllegalArgumentException("cronExpression must not be empty for CRON job: " + name);
            }
        }
    }

    /** 편의 메서드: 고정 주기 모드로 전환하며 값 지정 */
    public void useFixedRate(int initialDelay, int interval, TimeUnit unit) {
        this.initialDelay = initialDelay;
        this.interval = interval;
        this.timeUnit = unit;
        this.scheduleMode = JOB_SCHEDULE_MODE.FIXED_RATE;
    }

    /** 편의 메서드: 크론 모드로 전환하며 크론/타임존 지정 */
    public void useCron(String cronExpression, TimeZone timeZone) {
        this.cronExpression = cronExpression;
        this.timeZone = timeZone;
        this.scheduleMode = JOB_SCHEDULE_MODE.CRON;
    }

    /** 크론 모드인지 여부 */
    public boolean isCron() {
        return this.scheduleMode == JOB_SCHEDULE_MODE.CRON;
    }

    @Override
    public String toString() {
        return "Job{" +
                "name='" + name + '\'' +
                ", scheduleMode=" + scheduleMode +
                ", cronExpression='" + cronExpression + '\'' +
                ", timeZone=" + (timeZone != null ? timeZone.getID() : null) +
                ", initialDelay=" + initialDelay +
                ", interval=" + interval +
                ", timeUnit=" + timeUnit +
                ", priority=" + priority +
                ", totalRunCount=" + totalRunCount +
                ", curRemainRunCount=" + curRemainRunCount.get() +
                ", isLasted=" + isLasted +
                ", isFinished=" + isFinished.get() +
                ", scheduleUnitKey=" + scheduleUnitKey +
                '}';
    }

}
