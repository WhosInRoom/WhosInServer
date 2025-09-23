package com.WhoIsRoom.WhoIs_Server.global.common.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;


import java.time.LocalDateTime;

@Getter
@JsonPropertyOrder({"success", "status", "message", "timestamp"})
public class BaseErrorResponse{
    private final boolean success;
    private final int status;
    private final String message;
    private final LocalDateTime timestamp;

    public BaseErrorResponse(ErrorCode code) {
        this.success = false;
        this.status = code.getHttpStatus();
        this.message = code.getMessage();
        this.timestamp = LocalDateTime.now();
    }

    public BaseErrorResponse(ErrorCode code, String customMessage) {
        this.success = false;
        this.status = code.getHttpStatus();
        this.message = customMessage;
        this.timestamp = LocalDateTime.now();
    }
}

