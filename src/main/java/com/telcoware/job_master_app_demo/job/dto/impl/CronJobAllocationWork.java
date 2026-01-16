package com.telcoware.job_master_app_demo.job.dto.impl;

import com.telcoware.job_master_app_demo.data.definition.STRING_CONSTANTS;
import com.telcoware.job_master_app_demo.data.dto.job.JobInfoDto;
import com.telcoware.job_master_app_demo.data.dto.redis.KvPair;
import com.telcoware.job_master_app_demo.job.definition.JOB_STATUS_TYPE;
import com.telcoware.job_master_app_demo.job.dto.inf.JobInfo;
import com.telcoware.job_master_app_demo.job.dto.status.JobStatusInfoDto;
import com.telcoware.job_master_app_demo.scheduler.job.Job;
import com.telcoware.job_master_app_demo.scheduler.job.JobContainer;
import com.telcoware.job_master_app_demo.service.aws.elasticache.AwsValKeyService;
import com.telcoware.job_master_app_demo.service.grpc.GrpcClientService;
import com.telcoware.job_master_app_demo.service.job.JobReporter;
import com.telcoware.job_master_app_demo.util.GsonUtil;
import com.telcoware.job_master_app_demo.util.SnowflakeIdUtil;
import com.telcoware.job_master_app_demo.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.telcoware.job_master_app_demo.data.definition.STRING_CONSTANTS.JOB_PREFIX;

/**
 * packageName    : com.telcoware.job_master_app_demo.data.dto.job
 * fileName       : CronJobAllocationWork
 * author         : samuel
 * date           : 25. 10. 21.
 * description    :
 * - "크론 주기성 작업"을 워커 노드에 안전하게 할당하는 책임을 가진 컨테이너형 작업 클래스
 * - 실행 직전, 동일 이름의 작업(jobName)이 "이미 실행 중(RUNNING)"인지 Valkey(또는 Redis)에서 조회
 * - 중복 RUNNING 작업이 있으면 먼저 해당 작업 중지를 워커에게 gRPC로 요청한 뒤(StopJobInfo),
 * 신규 작업을 생성/할당(AddJobInfo)한다
 * <p>
 * 동작 개요:
 * 1) start() 호출 시, 내부 Job에 Runnable을 주입하여 스케줄러가 실행할 실제 로직을 구성
 * 2) 실행 시점에 checkDuplicatedJob()으로 중복 실행 방지
 * 3) 중복이 없거나(혹은 중지 요청 이후) 신규 jobId를 생성하여 상태를 HOLDING으로 기록
 * 4) 워커에게 AddJobInfo gRPC 호출로 실제 실행을 요청
 * <p>
 * 주의:
 * - Valkey fetch 패턴 및 직렬화 포맷(JobStatusInfoDto JSON)은 워커/마스터가 합의된 스키마 사용
 * - 네트워크 오류나 일시적 장애 시 재시도 전략은 상위 스케줄러/프레임워크 레벨에서 보완 필요
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 10. 21.        samuel       최초 생성
 */
@Slf4j
public class CronJobAllocationWork extends JobContainer {

    /**
     * 이번 할당의 타깃 작업 이름 (크론 잡 식별자)
     */
    private final String jobName;

    /**
     * 라운드로빈으로 워커 노드에 gRPC 호출을 보내는 클라이언트
     */
    private final GrpcClientService grpcClientService;

    /**
     * 작업 상태(HOLDING/RUNNING/.. )를 기록/갱신하는 리포터
     */
    private final JobReporter jobReporter;

    /**
     * Valkey/Redis 접근 서비스 (SCAN/MGET 등)
     */
    private final AwsValKeyService awsValKeyService;

    /**
     * JobStatusInfoDto 직렬화/역직렬화 유틸
     */
    private final GsonUtil gsonUtil;

    /**
     * @param job               스케줄러 프레임워크의 Job 래퍼(실행 타이밍/주기 관리)
     * @param jobName           크론 잡 이름(동일 이름끼리 중복 실행 방지 대상)
     * @param grpcClientService 워커로 JobInfo 전달용 gRPC 클라이언트
     * @param jobReporter       작업 상태 기록/천이 담당
     * @param awsValKeyService  작업 상태를 보관 중인 Valkey 접근기
     * @param gsonUtil          JSON <-> DTO 변환기
     */
    public CronJobAllocationWork(Job job,
                                 String jobName,
                                 GrpcClientService grpcClientService,
                                 JobReporter jobReporter,
                                 AwsValKeyService awsValKeyService,
                                 GsonUtil gsonUtil) {
        setJob(job); // 부모 컨테이너에 Job 주입

        this.jobName = jobName;
        this.grpcClientService = grpcClientService;
        this.jobReporter = jobReporter;
        this.awsValKeyService = awsValKeyService;
        this.gsonUtil = gsonUtil;
    }

    /**
     * 스케줄러가 호출할 실행 본문을 등록한다.
     * - 중복 실행 점검 -> 필요 시 중지 요청 -> 신규 작업 등록 순서
     * - 네트워크/직렬화 등 모든 예외를 잡아 경고 로그로 남기고 다음 주기에 재시도되도록 한다.
     */
    public void start() {
        getJob().setRunnable(() -> {
            try {
                // 1) 동일 jobName의 최신 상태를 스캔하여 중복 RUNNING이면 중지 요청
                checkDuplicatedJob();

                // 2) 신규 jobId 발급 (시간 기반 SnowflakeIdUtil 사용)
                String jobId = JOB_PREFIX.getValue() + SnowflakeIdUtil.nextId();

                // 3) Job 상태 천이: 신규 생성 직후에는 HOLDING(대기) 상태로 기록
                //    - 워커가 실제 RUNNING으로 바꾸는 시점은 워커 측 책임
                /*jobReporter.updateJobStatusInfoDto(
                        null, jobId, jobName,
                        JOB_STATUS_TYPE.HODLING
                );*/

                // 4) 워커에게 "작업 추가" 명령을 gRPC로 전송
                JobInfo<JobInfoDto> addJobInfo = new AddJobInfo(0L);
                addJobInfo.setBody(
                        JobInfoDto.builder()
                                .jobId(jobId)
                                .jobName(jobName)
                                .build()
                );
                grpcClientService.sendMessageByRoundRobin(addJobInfo);
            } catch (Exception e) {
                // 어떤 예외든 현재 주기를 실패로 두고 다음 주기에 재시도되도록 한다.
                log.warn("->SVC::Cron allocation failed. jobName={}", jobName, e);
            }
        });
    }

    /**
     * 현재 Valkey에서 job:* 키들을 조회하여,
     * - 동일 jobName 중 "가장 최근(updateDatetime 최대)" 상태를 집계
     * - 상태가 RUNNING이면 워커에게 StopJobInfo를 전송하여 중단을 요청
     * <p>
     * 구현 포인트:
     * - updateDatetime은 사전식 비교 가능한 문자열 포맷이어야 함(예: 2025-10-29T12:34:56)
     * - 다수 키 중 동일 jobName이 여러 개 있더라도 "가장 최근" 하나만 의미 있게 본다
     * - RUNNING이 아니면 신규 작업 할당을 계속 진행
     */
    private void checkDuplicatedJob() {
        // 현재 시스템에 존재하는 모든 job:* 키를 가져온다
        List<KvPair> fetchedKeyValues = awsValKeyService.fetchKeyValues(STRING_CONSTANTS.JOB_PREFIX_PATTERN.getValue());
        if (fetchedKeyValues != null && !fetchedKeyValues.isEmpty()) {
            for (KvPair kvPair : fetchedKeyValues) {
                if (kvPair == null) {
                    continue; // 널 세이프
                }

                String value = kvPair.value();
                if (!StringUtils.hasText(value)) {
                    continue; // 값이 없으면 스킵
                }

                // 저장된 JSON을 JobStatusInfoDto로 역직렬화
                JobStatusInfoDto jobStatusInfoDto = gsonUtil.deserialize(value, JobStatusInfoDto.class);
                if (jobStatusInfoDto == null) {
                    continue; // 역직렬화 실패 시 스킵
                }

                String currentJobName = jobStatusInfoDto.getJobName();
                if (!jobName.equals(currentJobName)) {
                    continue; // 이번에 처리하는 대상 jobName과 다르면 무시
                }

                String workerId = jobStatusInfoDto.getWorkerId();
                if (!StringUtils.hasText(workerId)) {
                    continue;
                }

                log.info("\t->SVC::[JobStatusInfoDto] (jobName={}) {}", jobName, gsonUtil.serialize(jobStatusInfoDto));

                String jobId = jobStatusInfoDto.getJobId();
                JOB_STATUS_TYPE jobStatusType = JOB_STATUS_TYPE.fromCode(jobStatusInfoDto.getStatus());
                if (jobStatusType == null) {
                    // 알 수 없는 상태코드면 스킵
                    continue;
                }

                // 이번에 실행하려는 jobName이 이미 RUNNING이면, 워커에게 중지 요청을 먼저 보낸다
                if (JOB_STATUS_TYPE.RUNNING.equals(jobStatusType)) {
                    log.warn("\t>SVC::[DELAYED CRON JOB !!!] jobId={}, jobName={}, status={}",
                            jobId, this.jobName, jobStatusType.getName());

                    // 워커에게 "해당 jobId 중지" 요청
                    JobInfo<JobInfoDto> stopJobInfo = new StopJobInfo(0L);
                    stopJobInfo.setBody(
                            JobInfoDto.builder()
                                    .jobId(jobId)
                                    .jobName(this.jobName)
                                    .build()
                    );
                    grpcClientService.sendMessage(
                            jobStatusInfoDto.getWorkerId(),
                            stopJobInfo
                    );
                }
            }
        }
    }

}