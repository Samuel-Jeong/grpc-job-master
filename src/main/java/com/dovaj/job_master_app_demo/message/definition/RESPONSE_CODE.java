package com.dovaj.job_master_app_demo.message.definition;

import lombok.Getter;

/**
 * packageName    : com.dovaj.job_master_app_demo.message.definition
 * fileName       : RESPONSE_CODE
 * author         : samuel
 * date           : 25. 10. 21.
 * description    : 메세지 응답 코드 enum 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 10. 21.        samuel       최초 생성
 */
@Getter
public enum RESPONSE_CODE {

    SUCCESS(0, "성공"),
    FAIL(-1, "실패"),

    ;

    private final Integer code;
    private final String message;

    RESPONSE_CODE(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
