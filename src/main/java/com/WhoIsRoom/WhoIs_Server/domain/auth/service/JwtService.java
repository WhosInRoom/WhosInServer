package com.WhoIsRoom.WhoIs_Server.domain.auth.service;

import com.WhoIsRoom.WhoIs_Server.domain.auth.dto.request.RefreshTokenRequest;
import com.WhoIsRoom.WhoIs_Server.domain.auth.dto.response.LoginResponse;
import com.WhoIsRoom.WhoIs_Server.domain.auth.dto.response.ReissueResponse;
import com.WhoIsRoom.WhoIs_Server.domain.auth.exception.CustomAuthenticationException;
import com.WhoIsRoom.WhoIs_Server.domain.auth.exception.CustomJwtException;
import com.WhoIsRoom.WhoIs_Server.domain.auth.util.JwtUtil;
import com.WhoIsRoom.WhoIs_Server.global.common.redis.RedisService;
import com.WhoIsRoom.WhoIs_Server.global.common.response.BaseResponse;
import com.WhoIsRoom.WhoIs_Server.global.common.response.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.access.expiration}")
    private Long ACCESS_TOKEN_EXPIRED_IN;

    @Value("${jwt.refresh.expiration}")
    private Long REFRESH_TOKEN_EXPIRED_IN;

    @Value("${jwt.access.header}")
    private String ACCESS_HEADER;

    @Value("${jwt.refresh.header}")
    private String REFRESH_HEADER;

    private static final String LOGOUT_VALUE = "logout";
    private static final String REFRESH_TOKEN_KEY_PREFIX = "auth:refresh:";
    private final String BEARER_PREFIX = "Bearer ";

    private final RedisService redisService;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public void logout(HttpServletRequest request, RefreshTokenRequest tokenRequest) {
        String accessToken = jwtUtil.extractAccessToken(request)
                .orElseThrow(() -> new CustomAuthenticationException(ErrorCode.SECURITY_INVALID_ACCESS_TOKEN));

        String refreshToken = tokenRequest.getRefreshToken();
        jwtUtil.validateToken(refreshToken);
        if (!"refresh".equals(jwtUtil.getTokenType(refreshToken))) {
            throw new CustomJwtException(ErrorCode.INVALID_TOKEN_TYPE);
        }

        deleteRefreshToken(refreshToken);
        //access token blacklist 처리 -> 로그아웃한 사용자가 요청 시 access token이 redis에 존재하면 jwtAuthenticationFilter에서 인증처리 거부
        invalidAccessToken(accessToken);
    }

    public ReissueResponse reissueTokens(RefreshTokenRequest tokenRequest) {
        String refreshToken = tokenRequest.getRefreshToken();
        jwtUtil.validateToken(refreshToken);
        if (!"refresh".equals(jwtUtil.getTokenType(refreshToken))) {
            throw new CustomJwtException(ErrorCode.INVALID_TOKEN_TYPE);
        }
        return reissueAndSendTokens(refreshToken);
    }

    public void checkLogout(String accessToken) {
        String value = redisService.getValues(accessToken);
        if (value.equals(LOGOUT_VALUE)) {
            throw new CustomAuthenticationException(ErrorCode.SECURITY_UNAUTHORIZED);
        }
    }

    public void storeRefreshToken(String refreshToken) {
        redisService.setValues(REFRESH_TOKEN_KEY_PREFIX, refreshToken, Duration.ofMillis(REFRESH_TOKEN_EXPIRED_IN));
    }

    private void deleteRefreshToken(String refreshToken){
        if(refreshToken == null){
            throw new CustomJwtException(ErrorCode.EMPTY_REFRESH_HEADER);
        }
        redisService.delete(refreshToken);
    }

    private void invalidAccessToken(String accessToken) {
        redisService.setValues(accessToken, LOGOUT_VALUE,
                Duration.ofMillis(ACCESS_TOKEN_EXPIRED_IN));
    }

    private ReissueResponse reissueAndSendTokens(String refreshToken) {

        // 새로운 Refresh Token 발급
        String reissuedAccessToken = jwtUtil.createAccessToken(jwtUtil.getUserId(refreshToken), jwtUtil.getProviderId(refreshToken), jwtUtil.getRole(refreshToken), jwtUtil.getName(refreshToken));
        String reissuedRefreshToken = jwtUtil.createRefreshToken(jwtUtil.getUserId(refreshToken), jwtUtil.getProviderId(refreshToken), jwtUtil.getName(refreshToken));

        // 새로운 Refresh Token을 DB나 Redis에 저장
        storeRefreshToken(reissuedRefreshToken);

        // 기존 Refresh Token 폐기 (DB나 Redis에서 삭제)
        deleteRefreshToken(refreshToken);

        return ReissueResponse.builder()
                .accessToken(reissuedAccessToken)
                .refreshToken(reissuedRefreshToken)
                .build();
    }
}
