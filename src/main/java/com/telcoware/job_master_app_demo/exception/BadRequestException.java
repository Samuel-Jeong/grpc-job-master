package com.telcoware.job_master_app_demo.exception;

import com.telcoware.job_master_app_demo.message.definition.SERVICE_RESPONSE;
import lombok.Getter;

/**
 * packageName    : com.sks.wpm.core.exception
 * fileName       : BadRequestException
 * author         : samuel
 * date           : 25. 4. 9.
 * description    : 잘못된 요청 오류 응답 예외 정의 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 9.        samuel       최초 생성
 */
@Getter
public class BadRequestException extends ServiceException {

    public BadRequestException(SERVICE_RESPONSE serviceResponse) {
        super(serviceResponse.getCode(), serviceResponse.getMessage());
    }

}
