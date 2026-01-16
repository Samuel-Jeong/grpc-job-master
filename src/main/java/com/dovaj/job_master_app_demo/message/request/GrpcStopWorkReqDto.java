package com.dovaj.job_master_app_demo.message.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.dovaj.job_master_app_demo.message.request
 * fileName       : GrpcStopWorkReqDto
 * author         : samuel
 * date           : 25. 10. 22.
 * description    : GRPC 작업 중지 요청 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 10. 22.        samuel       최초 생성
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrpcStopWorkReqDto {

    @NotNull(message = "jobId")
    private String jobId;

    @NotNull(message = "jobName")
    private String jobName;

}
