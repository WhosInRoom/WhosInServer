package com.WhoIsRoom.WhoIs_Server.domain.auth.filter;

import com.WhoIsRoom.WhoIs_Server.domain.auth.dto.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
public class CustomLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper;

    public CustomLoginFilter(AuthenticationManager authenticationManager,
                            ObjectMapper objectMapper,
                            AuthenticationSuccessHandler successHandler,
                            AuthenticationFailureHandler failureHandler) {
        this.objectMapper = objectMapper;
        super.setFilterProcessesUrl("/api/auth/login");
        super.setAuthenticationManager(authenticationManager);
        super.setAuthenticationSuccessHandler(successHandler);
        super.setAuthenticationFailureHandler(failureHandler);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        log.info("=== Login Filter (JSON only) 진입 ===");

        // 1) 메서드 강제: POST만 허용
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            throw new AuthenticationServiceException("지원하지 않는 HTTP 메서드입니다.");
        }

        // 2) Content-Type 강제: application/json만 허용
        String contentType = request.getContentType();
        if (contentType == null || !contentType.toLowerCase().contains("application/json")) {
            throw new AuthenticationServiceException("Content-Type application/json 만 허용됩니다.");
        }

        try {
            // 3) JSON 바디 파싱
            LoginRequest login = objectMapper.readValue(request.getInputStream(), LoginRequest.class);

            String email = login.getEmail();
            String password = login.getPassword();

            // 4) 값 검증 (비었으면 실패로 위임)
            if (email == null || email.isBlank() || password == null || password.isBlank()) {
                throw new BadCredentialsException("이메일/비밀번호가 비어있습니다.");
            }

            // 5) AuthenticationManager에게 위임
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(email.trim(), password);

            return this.getAuthenticationManager().authenticate(authToken);

        } catch (Exception e) {
            // 파싱 실패/기타 예외도 실패 핸들러로 위임
            throw new AuthenticationServiceException("로그인 요청 파싱 실패", e);
        }
    }
}
