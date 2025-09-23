package com.WhoIsRoom.WhoIs_Server.domain.auth.util;

import com.WhoIsRoom.WhoIs_Server.global.common.response.BaseErrorResponse;
import com.WhoIsRoom.WhoIs_Server.global.common.response.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class SecurityErrorResponseUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    public static void setErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getHttpStatus());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        BaseErrorResponse errorResponse = new BaseErrorResponse(errorCode);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
