package com.WhoIsRoom.WhoIs_Server.domain.auth.util;

import com.WhoIsRoom.WhoIs_Server.global.common.response.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private SecretKey secretKey;

    @Value("${jwt.access.expiration}")
    private Long ACCESS_TOKEN_EXPIRED_IN;

    @Value("${jwt.refresh.expiration}")
    private Long REFRESH_TOKEN_EXPIRED_IN;

    public final String BEARER = "Bearer ";

    public JwtUtil(@Value("${secret.jwt-secret-key}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Long getMemberId(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("memberId", Long.class);
    }

    public String getProviderId(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("providerId", String.class);
    }

    public String getRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    public String getTokenType(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("tokenType", String.class);
    }

    public String getEmail(String token){
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("email", String.class);
    }

    public String getName(String token){
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("name", String.class);
    }

    public Boolean isTokenExpired(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }

    public String createAccessToken(Long memberId, String providerId, String role, String name) {

        return Jwts.builder()
                .claim("tokenType", "access")
                .claim("memberId", memberId)
                .claim("providerId", providerId)
                .claim("role", role)
                .claim("name", name)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRED_IN))
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(Long memberId, String providerId, String role) {

        return Jwts.builder()
                .claim("tokenType", "refresh")
                .claim("memberId", memberId)
                .claim("providerId", providerId)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRED_IN))
                .signWith(secretKey)
                .compact();
    }

    public void validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) { // 토큰 만료
            throw new JwtException(ErrorCode.EXPIRED_ACCESS_TOKEN);
        } catch (UnsupportedJwtException e) { // 지원되지 않는 형식
            throw new JwtException(ErrorCode.UNSUPPORTED_TOKEN_TYPE);
        } catch (MalformedJwtException e) { // 구조가 잘못된 토큰
            throw new JwtException(ErrorCode.MALFORMED_TOKEN_TYPE);
        } catch (SignatureException e) { // 서명 위조 (곧 지원 중단)
            throw new JwtException(ErrorCode.INVALID_SIGNATURE_JWT);
        } catch (IllegalArgumentException e) { // 토큰이 비어 있거나 Null
            throw new JwtException(ErrorCode.EMPTY_AUTHORIZATION_HEADER);
        } catch (Exception e) { // 기타 예외 상황
            throw new JwtException(ErrorCode.INVALID_ACCESS_TOKEN);
        }
    }

    public String getUserNameFromToken(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("name", String.class);
    }

    public Optional<String> extractAccessToken(HttpServletRequest request, String accessHeader) {
        return Optional.ofNullable(request.getHeader(accessHeader))
                .filter(accessToken -> accessToken.startsWith(BEARER))
                .map(accessToken -> accessToken.replace(BEARER, ""));
    }

    public Optional<String> extractRefreshToken(HttpServletRequest request, String refreshHeader) {
        return Optional.ofNullable(request.getHeader(refreshHeader))
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(refreshToken -> refreshToken.replace(BEARER, ""));
    }
}
