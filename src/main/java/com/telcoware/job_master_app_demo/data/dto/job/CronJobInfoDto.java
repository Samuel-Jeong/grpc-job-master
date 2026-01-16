package com.telcoware.job_master_app_demo.data.dto.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/**
 * packageName    : com.telcoware.job_master_app_demo.job.dto.impl
 * fileName       : CronJobInfoDto
 * author         : samuel
 * date           : 25. 10. 28.
 * description    : 크론 작업 정보 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 10. 28.        samuel       최초 생성
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CronJobInfoDto {

    private String cron;

}
