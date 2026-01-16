package com.dovaj.job_master_app_demo.exception;

import com.dovaj.job_master_app_demo.message.definition.SERVICE_RESPONSE;
import lombok.Getter;

/**
 * packageName    : com.sks.wpm.core.exception
 * fileName       : NotFoundException
 * author         : samuel
 * date           : 25. 4. 9.
 * description    : 리소스 부재 오류 응답 예외 정의 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 4. 9.        samuel       최초 생성
 */
@Getter
public class NotFoundException extends ServiceException {

    public NotFoundException(SERVICE_RESPONSE serviceResponse) {
        super(serviceResponse.getCode(), serviceResponse.getMessage());
    }

}
