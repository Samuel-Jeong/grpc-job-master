package com.telcoware.job_master_app_demo.message.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * packageName    : com.telcoware.job_master_app_demo.message.request
 * fileName       : GrpcAddWorkReqDto
 * author         : samuel
 * date           : 25. 10. 22.
 * description    : GRPC 작업 추가 응답 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 10. 22.        samuel       최초 생성
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GrpcAddWorkResDto {

    private String message;

}
