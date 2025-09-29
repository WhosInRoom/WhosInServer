package com.WhoIsRoom.WhoIs_Server.domain.auth.handler.success;

import com.WhoIsRoom.WhoIs_Server.domain.auth.dto.response.LoginResponse;
import com.WhoIsRoom.WhoIs_Server.domain.auth.util.AuthenticationUtil;
import com.WhoIsRoom.WhoIs_Server.domain.auth.util.JwtUtil;
import com.WhoIsRoom.WhoIs_Server.domain.auth.service.JwtService;
import com.WhoIsRoom.WhoIs_Server.global.common.response.BaseErrorResponse;
import com.WhoIsRoom.WhoIs_Server.global.common.response.BaseResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
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
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        String providerId = authenticationUtil.getProviderId();
        String role = authenticationUtil.getRole();
        Long memberId = authenticationUtil.getMemberId();
        String nickName = authenticationUtil.getUsername();
        log.info("[CustomAuthenticationSuccessHandler] providerId={}, role={}, memberId={}", providerId, role, memberId);

        // 토큰 생성
        String accessToken = jwtUtil.createAccessToken(memberId, providerId, role, nickName);
        String refreshToken = jwtUtil.createRefreshToken(memberId, providerId, nickName);

        // refresh token 저장
        jwtService.storeRefreshToken(refreshToken);
        log.info("[CustomAuthenticationSuccessHandler], refreshToken={}", refreshToken);

        LoginResponse data = LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        BaseResponse<LoginResponse> body = BaseResponse.ok(data);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(response.getWriter(), body);
    }
}
