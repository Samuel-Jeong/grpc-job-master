package com.telcoware.job_master_app_demo.message.definition;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * packageName    : com.sks.wpm.core.data.definition.common
 * fileName       : SERVICE_RESPONSE
 * author         : samuel
 * date           : 25. 10. 21.
 * description    : 서비스 응답 정의 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 10. 21.       samuel       최초 생성
 */
@Getter
public enum SERVICE_RESPONSE {

    // COMMON
    SUCCESS(0, "성공"),
    CONTINUE(HttpStatus.CONTINUE.value(), "Continue"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST.value(), "잘못된 요청"),
    NOT_FOUND(HttpStatus.NOT_FOUND.value(), "찾을수 없음"),
    FORBIDDEN(HttpStatus.FORBIDDEN.value(), "금지됨"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED.value(), "승인되지 않음"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED.value(), "허용되지 않은 메서드 사용"),
    NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE.value(), "수용 불가"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "내부 서버 오류"),
    NOT_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED.value(), "Not Implemented"),
    BAD_GATEWAY(HttpStatus.BAD_GATEWAY.value(), "Bad Gateway"),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE.value(), "서비스 사용 불가"),
    GATEWAY_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT.value(), "Gateway Timeout"),
    TIME_FORMAT_CONVERSION_ERROR(1101, "시간 유형 변환 에러"),
    DATE_FORMAT_CONVERSION_ERROR(1102, "날짜 유형 변환 에러"),
    DATE_TIME_FORMAT_CONVERSION_ERROR(1103, "날짜 및 시간 유형 변환 에러"),
    BAD_REQUEST_AUTH_KEY(1104, "잘못된 인증 키 입니다."),

    ;

    private final int code;
    private final String message;

    SERVICE_RESPONSE(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static SERVICE_RESPONSE getServiceResponse(int code) {
        if (HttpStatus.OK.value() == code) {
            return SERVICE_RESPONSE.SUCCESS;
        }
        for (SERVICE_RESPONSE response : SERVICE_RESPONSE.values()) {
            if (response.getCode() == code) {
                return response;
            }
        }
        return SERVICE_RESPONSE.INTERNAL_SERVER_ERROR;
    }

    public String getFormattedMessage(String... args) {
        return String.format(message, (Object[]) args);
    }

}
