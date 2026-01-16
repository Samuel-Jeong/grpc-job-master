package com.telcoware.job_master_app_demo.job.handler;

import com.telcoware.job_master_app_demo.job.dto.inf.JobInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.telcoware.job_master_app_demo.job.handler
 * fileName       : JobWorker
 * author         : samuel
 * date           : 25. 10. 21.
 * description    : JOB 작업자
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 10. 21.        samuel       최초 생성
 */
@Slf4j
public class JobWorker<T> implements Runnable {

    private final JobInfo<T> jobInfo;

    public JobWorker(JobInfo<T> jobInfo) {
        this.jobInfo = jobInfo;
    }

    @Override
    public void run() {
        try {
            Long initialDelayMillis = jobInfo.getInitialDelayMillis();
            if (initialDelayMillis != null && initialDelayMillis > 0) {
                Thread.sleep(initialDelayMillis);
            }

            jobInfo.process();
        } catch (InterruptedException e) {
            // 스레드 인터럽트 복원 (중단 요청이 무시되지 않도록)
            Thread.currentThread().interrupt();
            log.warn("->SQS::worker interrupted during execution", e);
        } catch (Exception e) {
            log.warn("->SQS::worker run exception", e);
        }
    }

}
