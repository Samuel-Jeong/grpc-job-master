package com.dovaj.job_master_app_demo.message.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.dovaj.job_master_app_demo.exception.ServiceException;
import com.dovaj.job_master_app_demo.message.definition.RESPONSE_CODE;
import com.dovaj.job_master_app_demo.message.definition.SERVICE_RESPONSE;
import lombok.*;

import java.util.HashSet;
import java.util.Set;


/**
 * packageName    : com.sks.wpm.utils.message.dto
 * fileName       : DefaultResultDto
 * author         : kimer
 * date           : 2023-10-30
 * description    : 기본 응답 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-10-30        kimer       최초 생성
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@Getter
@Setter
@Builder
@ToString
@RequiredArgsConstructor(staticName = "of")
public class DefaultResultDto<D> {

    private final Integer resultCode;
    private final String resultMsg;

    private D data;

    private DefaultResultDto(int resultCode, String resultMsg) {
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
    }

    @JsonCreator
    private DefaultResultDto(@JsonProperty("resultCode") int resultCode, @JsonProperty("resultMsg") String resultMsg, @JsonProperty("data") D data) {
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
        this.data = data;
    }

    public static <D> DefaultResultDto<D> ofSuccess() {
        return new DefaultResultDto<>(RESPONSE_CODE.SUCCESS.getCode(), RESPONSE_CODE.SUCCESS.getMessage());
    }

    public static <D> DefaultResultDto<D> ofSuccess(D data) {
        return new DefaultResultDto<>(RESPONSE_CODE.SUCCESS.getCode(), RESPONSE_CODE.SUCCESS.getMessage(), data);
    }

    public static <D> DefaultResultDto<D> ofFail(int resultCode, String resultMsg) {
        return new DefaultResultDto<>(resultCode, resultMsg);
    }

    public static <D> DefaultResultDto<D> ofFail(int resultCode, String resultMsg, D data) {
        return new DefaultResultDto<>(resultCode, resultMsg, data);
    }

    public static <T> DefaultResultDtoBuilder<T> of(SERVICE_RESPONSE status) {
        return DefaultResultDto.<T>builder()
                .resultCode(status.getCode())
                .resultMsg(status.getMessage());
    }

    public static <T> DefaultResultDtoBuilder<T> ofError(ServiceException status) {
        return DefaultResultDto.<T>builder()
                .resultCode(status.getCode())
                .resultMsg(status.getMessage());
    }

    public static final Set<String> REQUIRED_FIELDS = new HashSet<>();

    static {
        REQUIRED_FIELDS.add("resultCode");
        REQUIRED_FIELDS.add("resultMsg");
    }

}

