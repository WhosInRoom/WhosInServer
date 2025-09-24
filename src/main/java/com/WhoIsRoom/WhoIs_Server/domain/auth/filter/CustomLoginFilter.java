package com.WhoIsRoom.WhoIs_Server.domain.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper;

    public CustomLoginFilter(AuthenticationManager authenticationManager,
                            ObjectMapper objectMapper,
                            AuthenticationSuccessHandler successHandler,
                            AuthenticationFailureHandler failureHandler) {
        this.objectMapper = objectMapper;

        // 기본 설정들 캡슐화
        super.setFilterProcessesUrl("/api/login"); // 로그인 엔드포인트 고정
        super.setUsernameParameter("email");       // username 대신 email
        super.setPasswordParameter("password");

        // AuthenticationManager + 핸들러 세팅
        super.setAuthenticationManager(authenticationManager);
        super.setAuthenticationSuccessHandler(successHandler);
        super.setAuthenticationFailureHandler(failureHandler);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        //클라이언트 요청에서 username, password 추출
        String email = obtainUsername(request);
        String password = obtainPassword(request);

        //스프링 시큐리티에서 username과 password를 검증하기 위해서는 token에 담아야 함
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password, null);

        //token에 담은 검증을 위한 AuthenticationManager로 전달
        return this.getAuthenticationManager().authenticate(authToken);
    }

    @Override
    protected String obtainUsername(HttpServletRequest request) {
        return request.getParameter("email"); // username 대신 email
    }
}
