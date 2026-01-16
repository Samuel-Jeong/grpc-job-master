package com.telcoware.job_master_app_demo.controller;

import com.telcoware.job_master_app_demo.data.dto.job.JobInfoDto;
import com.telcoware.job_master_app_demo.job.dto.impl.AddJobInfo;
import com.telcoware.job_master_app_demo.job.dto.impl.StopJobInfo;
import com.telcoware.job_master_app_demo.job.dto.inf.JobInfo;
import com.telcoware.job_master_app_demo.message.definition.SERVICE_RESPONSE;
import com.telcoware.job_master_app_demo.message.dto.DefaultResultDto;
import com.telcoware.job_master_app_demo.message.request.GrpcAddWorkReqDto;
import com.telcoware.job_master_app_demo.message.request.GrpcStopWorkReqDto;
import com.telcoware.job_master_app_demo.message.response.GrpcAddWorkResDto;
import com.telcoware.job_master_app_demo.service.grpc.GrpcClientService;
import com.telcoware.job_master_app_demo.util.SnowflakeIdUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.telcoware.job_master_app_demo.data.definition.STRING_CONSTANTS.JOB_PREFIX;

/**
 * packageName    : com.telcoware.job_master_app_demo.controller
 * fileName       : GrpcController
 * author         : samuel
 * date           : 25. 10. 21.
 * description    : GRPC 테스트 컨트롤러 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 10. 21.        samuel       최초 생성
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/grpc")
public class GrpcController {

    private final GrpcClientService grpcClientService;

    @PostMapping(value = "work")
    public DefaultResultDto<GrpcAddWorkResDto> addWork(@RequestBody @Valid GrpcAddWorkReqDto grpcAddWorkReqDto) {
        String jobId = JOB_PREFIX.getValue() + SnowflakeIdUtil.nextId();
        String jobName = grpcAddWorkReqDto.getJobName();

        JobInfo<JobInfoDto> addJobInfo = new AddJobInfo(0L);
        addJobInfo.setBody(
                JobInfoDto.builder()
                        .jobId(jobId)
                        .jobName(jobName)
                        .build()
        );
        grpcClientService.sendMessageByRoundRobin(addJobInfo);

        return DefaultResultDto.ofSuccess(
                GrpcAddWorkResDto.builder()
                        .message(SERVICE_RESPONSE.SUCCESS.getMessage())
                        .build()
        );
    }

    @PostMapping(value = "work/stop")
    public DefaultResultDto<GrpcAddWorkResDto> stopWork(@RequestBody @Valid GrpcStopWorkReqDto grpcStopWorkReqDto) {
        String jobId = grpcStopWorkReqDto.getJobId();
        String jobName = grpcStopWorkReqDto.getJobName();

        JobInfo<JobInfoDto> stopJobInfo = new StopJobInfo(0L);
        stopJobInfo.setBody(
                JobInfoDto.builder()
                        .jobId(jobId)
                        .jobName(jobName)
                        .build()
        );
        grpcClientService.sendMessageByRoundRobin(stopJobInfo);

        return DefaultResultDto.ofSuccess(
                GrpcAddWorkResDto.builder()
                        .message(SERVICE_RESPONSE.SUCCESS.getMessage())
                        .build()
        );
    }

}
