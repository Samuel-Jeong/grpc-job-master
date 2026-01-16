# Job Master App Demo

Spring Boot 기반의 작업 스케줄러/마스터 데모 애플리케이션입니다. 크론(CRON) 스케줄링, 라운드로빈 실행기(Executor) 큐, gRPC 연동(클라이언트), AWS ElastiCache(Valkey/Redis 호환) 예제를 포함합니다.

> 본 저장소는 데모/학습 목적의 코드로, 실제 서비스 적용 시에는 운영 환경 요구사항에 맞춘 보완이 필요합니다.

---

## 주요 기능
- CRON/주기성(Job interval) 작업 스케줄링
  - `org.springframework.scheduling.support.CronExpression` 사용
  - 5필드(UNIX) 또는 6필드(Quartz seconds 포함) 크론 표현식 지원 (연도 필드 7개인 경우 자동으로 연도 제거)
  - 타임존별 스케줄링(`Asia/Seoul` 등)
- 라운드로빈 방식의 다중 실행기(Executor) 큐
  - `JobScheduler` -> 여러 `JobExecutor` 인스턴스로 분배
- 작업 완료/취소 처리 및 콜백
- gRPC 클라이언트 예제 및 HTTP 엔드포인트(REST)로 트리거
- 프로필별(CI/로컬/운영 등) 설정, 크론 작업 정의 YAML 분리 로딩
- AWS ElastiCache(Valkey) 접근 예제(Lettuce)

---

## 기술 스택
- Java 17
- Spring Boot 3.5.x
- Web / WebFlux (실제 컨트롤러는 Web MVC 사용)
- gRPC (yidongnan/grpc-spring-boot-starter)
- Protobuf / protoc
- Lettuce (Valkey/Redis 클라이언트)
- Lombok, MapStruct, Gson, Guava, Apache Commons
- Gradle

---

## 프로젝트 구조(요약)
```
job-master-app-demo/
├─ build.gradle
├─ settings.gradle
├─ src/main/java/com/dovaj/job_master_app_demo/
│  ├─ JobMasterAppDemoApplication.java                    # Spring Boot 진입점
│  ├─ config/
│  │  ├─ CronJobConfig.java                               # 프로필별 cronjob-*.yml 로딩
│  │  ├─ JobConfig.java, ScheduleConfig.java, ...         # 설정 객체들
│  ├─ controller/GrpcController.java                      # POST /v1/api/grpc/work
│  ├─ scheduler/
│  │  ├─ job/Job.java, JobBuilder.java, JOB_SCHEDULE_MODE.java
│  │  ├─ schedule/
│  │  │  ├─ handler/JobScheduler.java, JobExecutor.java   # 스케줄/실행기
│  │  │  └─ unit/JobAdder.java                            # CRON/주기성 예약 로직
│  ├─ service/grpc/GrpcClientService.java                 # gRPC 클라이언트 예제
│  ├─ service/job/JobAllocator.java, JobTargetCollector.java
│  └─ redis/...                                           # Redis 연동 예제
├─ src/main/resources/
│  ├─ application.yml                                     # 공통 설정(프로필 연동)
│  ├─ application-local.yml                               # 로컬 프로필 설정
│  ├─ cronjob-local.yml                                   # 로컬용 CRON 작업 정의
│  ├─ bin/run.sh                                          # 배포 실행 스크립트 예시
│  └─ logback-spring.xml
└─ src/main/proto/worker.proto                            # gRPC 서비스 정의
```

---

## 핵심 컴포넌트 설명
### 스케줄러 흐름
- `JobScheduler`
  - 스케줄 유닛키, 풀사이즈, 큐사이즈로 초기화
  - `schedule(Job)` 호출 시 `JobAdder`를 통해 예약 시작, `JobExecutor`에 Round-Robin으로 작업 분배
  - `cancel(Job)`, `stop()` 지원
- `JobAdder`
  - CRON 작업일 경우 다음 실행 시점을 계산하여 예약, 실행 후 다음 스케줄을 재귀적으로 연결
  - Lasted/RemainCount 등의 상태에 따라 작업 종료/취소 처리
  - 5/6/7(연도 포함) 필드 크론 표현식에 대한 전처리(`sanitizeCronExpression`)
- `JobExecutor`
  - 내부 큐로 작업을 받아 실제 실행 담당

### CRON 작업 정의 로딩
- `CronJobConfig`가 애플리케이션 시작 시 `cronjob-${spring.profiles.active}.yml`을 읽어 `Map<String, LinkedHashMap<String, String>>` 형태로 보관
- 예: `cronjob-local.yml`
  ```yaml
  sync_user_status:
    cron: "*/1 * * * *"
    timezone: "Asia/Seoul"

  cleanup_expired_user_data:
    cron: "*/1 * * * *"
    timezone: "Asia/Seoul"
  ```

---

## 설정
### 프로필과 구성 파일
- `application.yml`에서 활성 프로필을 `${PROFILE}` 환경변수로 받습니다.
  - 예: `PROFILE=local`
- 프로필별 CRON 정의 파일을 자동 포함: `cronjob-${spring.profiles.active}.yml`
- 주요 포트/경로(로컬 기본값)
  - HTTP 포트: `8222` (`application-local.yml`)
  - gRPC 포트: `9090` (`application-local.yml`)

### AWS ElastiCache(Valkey)
- `application-local.yml`
  ```yaml
  aws:
    elasticache:
      valkey:
        endpoint:
          host: "clustercfg.dovaj-job-demo-cache.7csknd.apn2.cache.amazonaws.com"
          port: 6379
  ```
- 실제 환경에 맞게 `host`, `port`를 조정하세요.

### 스케줄러 스레드/큐 설정(예)
- `application-local.yml`
  ```yaml
  job:
    master:
      thread-pool:
        core-size: 5
        max-size: 10
        queue-capacity: 100
        watermark: 90

  schedule:
    target-collector.thread:
      pool-size: 5
      queue-size: 5
    job-allocator.thread:
      pool-size: 5
      queue-size: 5
  ```

---

## 빌드 및 실행
### 요구사항
- JDK 17+
- Gradle Wrapper 포함

### 로컬 실행 (권장)
```bash
# 1) 의존성 다운로드 및 빌드
./gradlew clean build

# 2) BootRun으로 실행 (PROFILE=local)
PROFILE=local ./gradlew bootRun
```
- 실행 후:
  - HTTP: http://localhost:8222
  - gRPC: localhost:9090 (HTTP/2)

### JAR로 실행
```bash
PROFILE=local java -jar build/libs/job-master-app-demo-0.0.1-SNAPSHOT.jar
```

### 배포 실행 스크립트(run.sh) 예시
- `src/main/resources/bin/run.sh` 참고
- 기본값으로 다음을 가정:
  - JAR 경로: `/home/ec2-user/dovaj/job-system/job-master-app/libs/job-master-app-demo-0.0.1-SNAPSHOT.jar`
  - JAVA 옵션 및 로그 경로 설정 포함
- 실제 배포 경로/권한에 맞게 수정하세요.

---

## 사용법
### gRPC 작업 트리거 REST API
- 엔드포인트: `POST /v1/api/grpc/work`
- 요청 바디(JSON)
  ```json
  {
    "key": "sample-key",
    "jobId": "optional-id-or-omit",
    "jobName": "sample-job"
  }
  ```
  - `key`: 필수
  - `jobName`: 필수
  - `jobId`: 미전달 시 서버에서 UUID 자동 생성

- 예시(curl)
  ```bash
  curl -X POST "http://localhost:8222/v1/api/grpc/work" \
       -H "Content-Type: application/json" \
       -d '{
             "key": "demo",
             "jobName": "hello-worker"
           }'
  ```

### gRPC 프로토콜 정의
- `src/main/proto/worker.proto`
  ```proto
  service WorkerService {
    rpc SendWork(SendWorkReq) returns (SendWorkRes);
  }

  message SendWorkReq {
    string id = 1;
    string name = 2;
  }
  message SendWorkRes {
    string message = 1;
  }
  ```

---

## 크론 스케줄 추가/변경 방법
1. 프로필에 맞는 파일을 편집 (예: 로컬은 `src/main/resources/cronjob-local.yml`)
2. 작업 키 아래에 `cron`, `timezone`을 정의
   ```yaml
   my_batch_job:
     cron: "0 */5 * * * *"    # 매 5분 0초
     timezone: "Asia/Seoul"
   ```
3. 애플리케이션 재시작

> 참고: 5필드(UNIX) 표현식인 경우 초(`seconds`)가 자동으로 `0`으로 보정됩니다. 7필드(연도 포함)인 경우 연도는 제거됩니다.

---

## 트러블슈팅
- 애플리케이션 시작 시 `Failed to load targetYmlName` 예외
  - `PROFILE` 환경변수가 설정되어 있는지 확인 (`local`, `dev`, `stg`, `live` 등)
  - 해당 프로필의 `cronjob-<profile>.yml`이 `src/main/resources`에 존재하는지 확인
- REST 호출 404/연결 실패
  - 로컬 포트(기본 8222)와 엔드포인트 경로(`/v1/api/grpc/work`) 확인
  - 방화벽/프록시 영향 확인
- gRPC 연결 문제
  - gRPC 서버 포트(기본 9090)와 대상 서비스 설정 확인
  - HTTP/2 및 TLS 설정 여부 점검(필요 시)
- Redis/Valkey 연결 실패
  - `application-*.yml`의 `aws.elasticache.valkey.endpoint` `host/port` 확인
  - 보안 그룹/네트워크 접근성 확인

---

## 라이선스
- 본 저장소는 별도의 라이선스 파일이 포함되어 있지 않습니다. 사내/프로젝트 정책에 맞게 라이선스를 지정하세요.

---

## 문의
- 담당자/팀: dovaj Job System Demo
- 로그/운영 경로: `application.yml`의 서버 액세스 로그, `run.sh`의 GC 로그 경로 참고
