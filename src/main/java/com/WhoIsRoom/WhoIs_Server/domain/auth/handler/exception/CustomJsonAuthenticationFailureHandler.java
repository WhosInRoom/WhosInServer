package com.WhoIsRoom.WhoIs_Server.domain.auth.handler.exception;

import com.WhoIsRoom.WhoIs_Server.global.common.response.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.WhoIsRoom.WhoIs_Server.domain.auth.util.SecurityErrorResponseUtil.setErrorResponse;

@Slf4j
@Component
public class CustomJsonAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException authenticationException) throws IOException {
        log.warn("=== AuthenticationFailureHandler 진입: type={}, msg={} ===",
                authenticationException.getClass().getSimpleName(), authenticationException.getMessage());

        ErrorCode code = mapToErrorCode(authenticationException);
        setErrorResponse(response, code);
    }

    private ErrorCode mapToErrorCode(AuthenticationException ex) {

        // 1) 아이디 없음
        if (ex instanceof UsernameNotFoundException) {
            return ErrorCode.SECURITY_UNAUTHORIZED;
        }

        // 2) 잘못된 자격 증명(값 누락/불일치)
        if (ex instanceof BadCredentialsException) {
            return ErrorCode.INVALID_EMAIL_OR_PASSWORD;
        }

        // 4) 요청 형식/메서드/파싱 문제 (JSON only 강제)
        if (ex instanceof AuthenticationServiceException) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "";

            if (msg.contains("HTTP 메서드")) {
                return ErrorCode.METHOD_NOT_ALLOWED;
            }
            if (msg.contains("Content-Type")) {
                return ErrorCode.ILLEGAL_ARGUMENT;
            }
            if (msg.contains("파싱")) {
                return ErrorCode.ILLEGAL_ARGUMENT;
            }
            // 그 외 서비스 예외는 일단 잘못된 요청으로 처리
            return ErrorCode.ILLEGAL_ARGUMENT;
        }

        // 5) 그 외 디폴트
        return ErrorCode.ILLEGAL_ARGUMENT;
    }
}