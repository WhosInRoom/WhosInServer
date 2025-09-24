package com.WhoIsRoom.WhoIs_Server.domain.auth.handler.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomJsonAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest req, HttpServletResponse res,
                                        AuthenticationException ex) throws IOException {
        res.setContentType("application/json;charset=UTF-8");

        int http = HttpServletResponse.SC_UNAUTHORIZED; // 401 기본
        String code = "INVALID_CREDENTIALS";
        String message = "이메일 또는 비밀번호가 올바르지 않습니다.";

        if (ex instanceof UsernameNotFoundException) {
            code = "USER_NOT_FOUND";
            message = ex.getMessage();
        } else if (ex instanceof LockedException) {
            http = 423; code = "ACCOUNT_LOCKED"; message = "잠긴 계정입니다.";
        } else if (ex instanceof DisabledException) {
            http = 403; code = "ACCOUNT_DISABLED"; message = "비활성화된 계정입니다.";
        } else if (ex instanceof AccountExpiredException) {
            http = 403; code = "ACCOUNT_EXPIRED"; message = "만료된 계정입니다.";
        } else if (ex instanceof CredentialsExpiredException) {
            code = "CREDENTIALS_EXPIRED"; message = "비밀번호가 만료되었습니다.";
        }

        res.setStatus(http);
        res.getWriter().write("""
        {"success":false,"code":"%s","message":"%s"}
        """.formatted(code, message));
    }
}