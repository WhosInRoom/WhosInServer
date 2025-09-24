package com.WhoIsRoom.WhoIs_Server.domain.auth.exception;

import com.WhoIsRoom.WhoIs_Server.global.common.response.ErrorCode;
import io.jsonwebtoken.JwtException;
import lombok.Getter;

@Getter
public class CustomJwtException extends JwtException {
    private final ErrorCode errorCode;

    public CustomJwtException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
