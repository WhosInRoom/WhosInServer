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

import java.io.IOException;

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

        // 1) 메서드 강제
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            throw new AuthenticationServiceException("지원하지 않는 HTTP 메서드입니다.");
        }

        // 2) Content-Type 강제
        String contentType = request.getContentType();
        if (contentType == null || !contentType.toLowerCase().contains("application/json")) {
            throw new AuthenticationServiceException("Content-Type application/json 만 허용됩니다.");
        }

        // 3) JSON 파싱만 try-catch
        LoginRequest login;
        try {
            login = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
        } catch (IOException e) {
            // ✅ 진짜 파싱 실패만 여기로
            throw new AuthenticationServiceException("JSON 파싱 실패", e);
        }

        String email = login.getEmail();
        String password = login.getPassword();

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            // ✅ 자격증명 문제는 그대로 던져서 FailureHandler가 처리
            throw new BadCredentialsException("이메일/비밀번호가 비어있습니다.");
        }

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(email.trim(), password);

        // ✅ 여기서 발생하는 UsernameNotFoundException/BadCredentialsException 등은
        //    래핑하지 말고 그대로 던진다 → FailureHandler에서 올바른 코드로 매핑됨
        return this.getAuthenticationManager().authenticate(authToken);
    }
}
