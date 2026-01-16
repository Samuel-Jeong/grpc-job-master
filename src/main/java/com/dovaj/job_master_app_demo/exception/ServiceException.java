package com.dovaj.job_master_app_demo.exception;

import com.dovaj.job_master_app_demo.message.definition.SERVICE_RESPONSE;
import lombok.Getter;
import lombok.ToString;

/**
 * packageName    : com.sks.wpm.core.exception
 * fileName       : ServiceException
 * author         : samuel
 * date           : 24. 10. 30.
 * description    : 서비스 예외 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 24. 10. 30.        samuel       최초 생성
 */
@ToString
public class ServiceException extends RuntimeException {

    @Getter
    private final Integer code;
    @Getter
    private final String message;

    public ServiceException(SERVICE_RESPONSE response) {
        super(String.format("%s", response.getMessage()));
        this.code = response.getCode();
        this.message = response.getMessage();
    }

    public ServiceException(Integer code, String message) {
        super(String.format("%s", message));
        this.code = code;
        this.message = message;
    }

}
