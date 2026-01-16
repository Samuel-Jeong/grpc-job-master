package com.dovaj.job_master_app_demo.exception;

import com.dovaj.job_master_app_demo.message.definition.SERVICE_RESPONSE;
import lombok.Getter;

/**
 * packageName    : com.sks.wpm.core.exception
 * fileName       : InternalServerException
 * author         : samuel
 * date           : 25. 4. 9.
 * description    : 서버 내부 오류 응답 예외 정의 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 9.        samuel       최초 생성
 */
@Getter
public class InternalServerException extends ServiceException {

    public InternalServerException(SERVICE_RESPONSE serviceResponse) {
        super(serviceResponse.getCode(), serviceResponse.getMessage());
    }

}
