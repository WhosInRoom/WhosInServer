package com.WhoIsRoom.WhoIs_Server.domain.auth.handler.exception;

import com.WhoIsRoom.WhoIs_Server.global.common.response.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
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
        log.info("=== AuthenticationFailureHandler 진입 ===");

        ErrorCode code = ErrorCode.INVALID_ID_OR_PASSWORD;

        if (authenticationException instanceof UsernameNotFoundException) {
            code = ErrorCode.USER_NOT_FOUND;
        }
        setErrorResponse(response, code);
    }
}