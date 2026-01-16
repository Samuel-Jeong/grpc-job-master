package com.dovaj.job_master_app_demo.exception;

import com.dovaj.job_master_app_demo.message.definition.SERVICE_RESPONSE;
import lombok.Getter;

/**
 * packageName    : com.sks.wpm.core.exception
 * fileName       : UnAuthorizedException
 * author         : sein
 * date           : 2025-04-08
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-04-08        sein       최초 생성
 */
@Getter
public class UnAuthorizedException extends ServiceException {

    public UnAuthorizedException() {
        super(SERVICE_RESPONSE.UNAUTHORIZED);
    }

    public UnAuthorizedException(SERVICE_RESPONSE serviceResponse) {
        super(serviceResponse.getCode(), serviceResponse.getMessage());
    }

}
