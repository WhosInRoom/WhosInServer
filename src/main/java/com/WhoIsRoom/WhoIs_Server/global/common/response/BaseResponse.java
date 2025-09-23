package com.WhoIsRoom.WhoIs_Server.global.common.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@JsonPropertyOrder({"success", "status", "message", "data"})
public class BaseResponse<T> {

    private final boolean success;

    @Schema(example = "200")
    private final int status;

    @Schema(example = "요청에 성공하였습니다.")
    private final String message;
    private final T data;

    private BaseResponse(T data) {
        this.success = true;
        this.status = HttpStatus.OK.value();
        this.message = "요청에 성공하였습니다.";
        this.data = data;
    }

    public static <T> BaseResponse<T> ok(T data) {
        return new BaseResponse<>(data);
    }
}
