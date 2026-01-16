package com.telcoware.job_master_app_demo.exception;

import com.telcoware.job_master_app_demo.message.definition.SERVICE_RESPONSE;
import lombok.Getter;

/**
 * packageName    : com.sks.wpm.core.exception
 * fileName       : ForbiddenException
 * author         : sein
 * date           : 2025-04-08
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-04-08        sein       최초 생성
 */
@Getter
public class ForbiddenException extends ServiceException {

    public ForbiddenException() {
        super(SERVICE_RESPONSE.FORBIDDEN);
    }

    public ForbiddenException(SERVICE_RESPONSE serviceResponse) {
        super(serviceResponse.getCode(), serviceResponse.getMessage());
    }

}
