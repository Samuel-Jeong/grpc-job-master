package com.dovaj.job_master_app_demo.job.dto.impl;

import com.dovaj.job_master_app_demo.data.dto.grpc.GrpcConnectionInfo;
import com.dovaj.job_master_app_demo.data.dto.job.JobInfoDto;
import com.dovaj.job_master_app_demo.job.dto.inf.JobInfo;
import com.dovaj.job_master_app_demo.proto.StopWorkReq;
import com.dovaj.job_master_app_demo.proto.StopWorkRes;
import com.dovaj.job_master_app_demo.proto.WorkerServiceGrpc;
import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.dovaj.job_master_app_demo.job.dto.impl
 * fileName       : AddJobInfo
 * author         : samuel
 * date           : 25. 10. 22.
 * description    : 작업 전송 정보 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 10. 22.        samuel       최초 생성
 */
@Slf4j
public class StopJobInfo extends JobInfo<JobInfoDto> {

    public StopJobInfo(Long initialDelayMillis) {
        super(JobInfoDto.class);
        this.setInitialDelayMillis(initialDelayMillis);
    }

    @Override
    public void process() {
        GrpcConnectionInfo grpcServerInfo = getGrpcConnectionInfo();
        if (grpcServerInfo == null) {
            return;
        }

        JobInfoDto jobInfoDto = this.getBody();
        if (jobInfoDto == null) {
            return;
        }
        String jobId = jobInfoDto.getJobId();
        String jobName = jobInfoDto.getJobName();

        try {
            WorkerServiceGrpc.WorkerServiceBlockingStub blockingStub = grpcServerInfo.getBlockingStub();
            if (blockingStub == null) {
                return;
            }

            StopWorkRes stopWorkRes = blockingStub.stopWork(
                    StopWorkReq.newBuilder()
                            .setId(jobId)
                            .setName(jobName)
                            .build()
            );
            if (stopWorkRes != null) {
                log.info("->SVC::Success to stop work(id={}, name={}) to [{}]",
                        jobId, jobName, grpcServerInfo.toSummary()
                );
            } else {
                log.warn("->SVC::Fail to stop work(id={}, name={}) to [{}]",
                        jobId, jobName, grpcServerInfo.toSummary()
                );
            }
        } catch (Exception e) {
            log.warn("->SVC::[StopJobInfo] [Exception] {}", e.getMessage());
        }
    }

}
