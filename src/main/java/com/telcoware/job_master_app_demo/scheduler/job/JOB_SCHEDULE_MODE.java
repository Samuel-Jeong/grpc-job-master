package com.telcoware.job_master_app_demo.scheduler.job;

import lombok.Getter;

/**
 * packageName    : com.telcoware.job_master_app_demo.scheduler.job
 * fileName       : JOB_SCHEDULE_MODE
 * author         : samuel
 * date           : 25. 10. 28.
 * description    : 작업 스케줄 모드 enum 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 10. 28.        samuel       최초 생성
 */
@Getter
public enum JOB_SCHEDULE_MODE {

    FIXED_RATE((short) 0),
    CRON((short) 1),

    ;

    private final Short mode;

    JOB_SCHEDULE_MODE(Short mode) {
        this.mode = mode;
    }

}
