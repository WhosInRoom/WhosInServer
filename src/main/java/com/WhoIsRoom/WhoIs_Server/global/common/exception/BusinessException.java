package com.WhoIsRoom.WhoIs_Server.global.common.exception;

import com.WhoIsRoom.WhoIs_Server.global.common.response.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
