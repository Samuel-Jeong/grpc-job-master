package com.dovaj.job_master_app_demo.job.dto.impl;

import com.dovaj.job_master_app_demo.data.definition.STRING_CONSTANTS;
import com.dovaj.job_master_app_demo.data.dto.grpc.GrpcConnectionInfo;
import com.dovaj.job_master_app_demo.data.dto.pod.WorkerInfo;
import com.dovaj.job_master_app_demo.data.dto.redis.KvPair;
import com.dovaj.job_master_app_demo.scheduler.job.Job;
import com.dovaj.job_master_app_demo.scheduler.job.JobContainer;
import com.dovaj.job_master_app_demo.service.aws.elasticache.AwsValKeyService;
import com.dovaj.job_master_app_demo.service.grpc.GrpcClientService;
import com.dovaj.job_master_app_demo.util.GsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * packageName    : com.dovaj.job_master_app_demo.data.dto.job
 * fileName       : JobTargetSelectionWork
 * author         : samuel
 * date           : 25. 10. 21.
 * description    : 작업 대상 선택 로직 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 10. 21.        samuel       최초 생성
 */
@Slf4j
public class JobTargetSelectionWork extends JobContainer {

    private final GrpcClientService grpcClientService;
    private final AwsValKeyService awsValKeyService;
    private final GsonUtil gsonUtil;

    public JobTargetSelectionWork(Job job,
                                  GrpcClientService grpcClientService,
                                  AwsValKeyService awsValKeyService,
                                  GsonUtil gsonUtil) {
        setJob(job);

        this.grpcClientService = grpcClientService;
        this.awsValKeyService = awsValKeyService;
        this.gsonUtil = gsonUtil;
    }

    public void start() {
        getJob().setRunnable(() -> {
            try {
                List<KvPair> fetchedKeyValues = awsValKeyService.fetchKeyValues(STRING_CONSTANTS.WORKER_PREFIX_PATTERN.getValue());
                if (fetchedKeyValues == null) {
                    return;
                }

                List<String> fetchedKeys = new ArrayList<>();
                for (int i = 0; i < fetchedKeyValues.size(); i++) {
                    KvPair kvPair = fetchedKeyValues.get(i);
                    if (kvPair == null) {
                        continue;
                    }

                    String workerId = kvPair.key();
                    String value = kvPair.value();
                    if (log.isDebugEnabled()) {
                        log.debug("->SVC::[{}] [key={}, value={}]", i, workerId, value);
                    }

                    WorkerInfo workerInfo = gsonUtil.deserialize(value, WorkerInfo.class);
                    if (workerInfo != null) {
                        GrpcConnectionInfo grpcServerInfo = grpcClientService.getGrpcServerInfo(workerId);
                        if (grpcServerInfo != null) {
                            int port = grpcServerInfo.getPort();
                            if (port != workerInfo.getGrpcPort()) {
                                grpcClientService.addGrpcServerInfo(workerId, workerInfo.getGrpcPort());
                            }
                        } else {
                            grpcClientService.addGrpcServerInfo(workerId, workerInfo.getGrpcPort());
                        }
                    }

                    fetchedKeys.add(workerId);
                }

                // 조회한 목록의 파드 정보들의 키가 클라이언트 목록에 없으면 해당 클라이언트 목록에서 삭제
                grpcClientService.checkGrpcServerInfo(fetchedKeys);
            } catch (Exception e) {
                log.warn("->SVC::", e);
            }
        });
    }

}
