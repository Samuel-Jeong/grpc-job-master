package com.dovaj.job_master_app_demo.service.grpc;

import com.dovaj.job_master_app_demo.data.dto.grpc.GrpcConnectionInfo;
import com.dovaj.job_master_app_demo.exception.InternalServerException;
import com.dovaj.job_master_app_demo.job.dto.inf.JobInfo;
import com.dovaj.job_master_app_demo.job.handler.JobMaster;
import com.dovaj.job_master_app_demo.message.definition.SERVICE_RESPONSE;
import com.dovaj.job_master_app_demo.proto.WorkerServiceGrpc;
import io.grpc.ManagedChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * packageName    : com.dovaj.job_master_app_demo.service.grpc
 * fileName       : GrpcClientService
 * author         : samuel
 * date           : 25. 10. 22.
 * description    : GRPC Client 서비스 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 10. 22.        samuel       최초 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GrpcClientService {

    private static final String FIXED_TARGET_HOST = "localhost";

    /**
     * KEY : WORKER ID
     * VALUE : GRPC 연결 정보
     */
    private final Map<String, GrpcConnectionInfo> grpcConnectionInfoMap = new ConcurrentHashMap<>();
    private final AtomicInteger index = new AtomicInteger(0);

    private final JobMaster jobMaster;

    public void addGrpcServerInfo(String key, int port) {
        if (!StringUtils.hasText(key) || port < 0 || port > 65535) {
            return;
        }

        ManagedChannel channel = io.grpc.ManagedChannelBuilder
                .forAddress(FIXED_TARGET_HOST, port)
                .usePlaintext()
                .build();
        WorkerServiceGrpc.WorkerServiceBlockingStub blockingStub = WorkerServiceGrpc.newBlockingStub(channel);

        grpcConnectionInfoMap.put(
                key,
                GrpcConnectionInfo.builder()
                        .host(FIXED_TARGET_HOST)
                        .port(port)
                        .channel(channel)
                        .blockingStub(blockingStub)
                        .build()
        );

        log.info("->SVC::[GRPC CONNECTION MAP] {}", formatGrpcMapOneLine());
    }

    private String formatGrpcMapOneLine() {
        // 스냅샷 생성(동시성 안정성 + 일관된 출력)
        Map<String, GrpcConnectionInfo> snapshot = Map.copyOf(grpcConnectionInfoMap);

        if (snapshot.isEmpty()) {
            return "size=0 []";
        }

        String body = snapshot.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    GrpcConnectionInfo.Summary s = e.getValue().toSummary();
                    return String.format("%s=%s:%d(%s)", e.getKey(), s.getHost(), s.getPort(), s.getState());
                })
                .collect(Collectors.joining(", "));

        return String.format("size=%d [%s]", snapshot.size(), body);
    }

    public void removeGrpcServerInfo(String key) {
        grpcConnectionInfoMap.remove(key);

        log.info("->SVC::[GRPC CONNECTION MAP] {}", formatGrpcMapOneLine());
    }

    public GrpcConnectionInfo getGrpcServerInfo(String key) {
        return grpcConnectionInfoMap.get(key);
    }

    public void checkGrpcServerInfo(List<String> keys) {
        List<String> deletedKeys = new ArrayList<>();
        for (String key : grpcConnectionInfoMap.keySet()) {
            if (!StringUtils.hasText(key)) {
                continue;
            }
            if (!keys.contains(key)) {
                deletedKeys.add(key);
            }
        }
        for (String key : deletedKeys) {
            if (!StringUtils.hasText(key)) {
                continue;
            }
            removeGrpcServerInfo(key);
        }
    }

    /**
     * 라운드로빈으로 다음 Connection 선택
     */
    public GrpcConnectionInfo getNextConnection() {
        List<String> keys = grpcConnectionInfoMap.keySet().stream().sorted().toList();
        if (keys.isEmpty()) {
            return null;
        }

        int currentIndex = Math.abs(index.getAndIncrement() % keys.size());
        String selectedKey = keys.get(currentIndex);
        return grpcConnectionInfoMap.get(selectedKey);
    }

    public void sendMessageByRoundRobin(JobInfo<?> jobInfo) {
        if (jobInfo == null) {
            return;
        }

        sendMessage(getNextConnection(), jobInfo);
    }

    public void sendMessage(String workerId, JobInfo<?> jobInfo) {
        if (!StringUtils.hasText(workerId) || jobInfo == null) {
            return;
        }

        sendMessage(grpcConnectionInfoMap.get(workerId), jobInfo);
    }

    public void sendMessage(GrpcConnectionInfo grpcConnectionInfo, JobInfo<?> jobInfo) {
        if (grpcConnectionInfo == null || jobInfo == null) {
            log.warn("->SVC::grpcConnectionInfo({}) is null or jobInfo({}) is null.", grpcConnectionInfo, jobInfo);
            return;
        }
        jobInfo.setGrpcConnectionInfo(grpcConnectionInfo);

        try {
            if (jobMaster.isActive()) {
                jobMaster.assignJob(jobInfo);
            } else { // Job system 이 active 가 아니면 > Throw SERVICE_UNAVAILABLE (503)
                throw new InternalServerException(SERVICE_RESPONSE.SERVICE_UNAVAILABLE);
            }
        } catch (Exception e) {
            log.warn("->SVC::sendMessage >>> {}", e.getMessage());
            throw new InternalServerException(SERVICE_RESPONSE.INTERNAL_SERVER_ERROR);
        }
    }

}
