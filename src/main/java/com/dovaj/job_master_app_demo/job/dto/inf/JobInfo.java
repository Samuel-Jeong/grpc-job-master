package com.dovaj.job_master_app_demo.job.dto.inf;

import com.dovaj.job_master_app_demo.data.dto.grpc.GrpcConnectionInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * packageName    : com.dovaj.job_master_app_demo.job.dto
 * fileName       : JobInfo
 * author         : samuel
 * date           : 25. 10. 21.
 * description    : JOB 정보
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 10. 21.        samuel       최초 생성
 */
@Slf4j
@Data
@AllArgsConstructor
public abstract class JobInfo<T> {

    // 메세지
    private final Class<T> bodyClass;

    // GRPC 연결 정보
    private GrpcConnectionInfo grpcConnectionInfo;

    // JOB 시작 딜레이 (단위: ms)
    private Long initialDelayMillis;
    private T body;
    private String gsonName = "";

    public JobInfo(Class<T> bodyClass) {
        this.bodyClass = bodyClass;
    }

    // 비즈니스 로직
    abstract public void process();

}
