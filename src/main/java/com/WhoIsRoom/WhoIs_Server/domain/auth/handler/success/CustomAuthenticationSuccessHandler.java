package com.WhoIsRoom.WhoIs_Server.domain.auth.handler.success;

import com.WhoIsRoom.WhoIs_Server.domain.auth.util.AuthenticationUtil;
import com.WhoIsRoom.WhoIs_Server.domain.auth.util.JwtUtil;
import com.WhoIsRoom.WhoIs_Server.domain.auth.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthenticationUtil authenticationUtil;
    private final JwtUtil jwtUtil;
    private final JwtService jwtService;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        String providerId = authenticationUtil.getProviderId();
        String role = authenticationUtil.getRole();
        Long memberId = authenticationUtil.getMemberId();
        String userName = authenticationUtil.getUsername();
        String email = authenticationUtil.getEmail();
        log.info("[CustomAuthenticationSuccessHandler] providerId={}, role={}, memberId={}, email={}", providerId, role, memberId, email);

        // 토큰 생성
        String accessToken = jwtUtil.createAccessToken(memberId, providerId, role, userName);
        String refreshToken = jwtUtil.createRefreshToken(memberId, providerId, role);

        // refresh token 저장
        jwtService.storeRefreshToken(refreshToken);
        log.info("[CustomAuthenticationSuccessHandler], refreshToken={}", refreshToken);

        // 리다이렉션
        response.sendRedirect(LOGIN_SUCCESS_URI);
    }
}
