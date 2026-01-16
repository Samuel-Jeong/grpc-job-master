package com.dovaj.job_master_app_demo.data.dto.grpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.dovaj.job_master_app_demo.proto.WorkerServiceGrpc;
import io.grpc.ManagedChannel;
import lombok.*;

/**
 * packageName    : com.dovaj.job_master_app_demo.data.dto.grpc
 * fileName       : GrpcConnectionInfo
 * author         : samuel
 * date           : 25. 10. 22.
 * description    : GRPC 서버 정보 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 10. 22.        samuel       최초 생성
 */
@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class GrpcConnectionInfo {

    private String host;
    private int port;
    private ManagedChannel channel;
    private WorkerServiceGrpc.WorkerServiceBlockingStub blockingStub;

    // 로그용 요약 뷰 객체
    public Summary toSummary() {
        String state = (channel != null) ? channel.getState(false).name() : "UNKNOWN";
        return new Summary(host, port, state);
    }

    @Getter
    @ToString
    public static class Summary {
        private final String host;
        private final int port;
        private final String state; // IDLE, CONNECTING, READY, TRANSIENT_FAILURE, SHUTDOWN
        public Summary(String host, int port, String state) {
            this.host = host;
            this.port = port;
            this.state = state;
        }
    }

}
