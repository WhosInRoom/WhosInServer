package com.WhoIsRoom.WhoIs_Server.domain.auth.util;

import com.WhoIsRoom.WhoIs_Server.domain.auth.exception.CustomJwtException;
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

    private final SecretKey secretKey;

    @Value("${jwt.access.expiration}")
    private Long ACCESS_TOKEN_EXPIRED_IN;

    @Value("${jwt.refresh.expiration}")
    private Long REFRESH_TOKEN_EXPIRED_IN;

    @Value("${jwt.access.header}")
    private String ACCESS_HEADER;

    @Value("${jwt.refresh.header}")
    private String REFRESH_HEADER;

    public final String BEARER_PREFIX = "Bearer ";

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Long getUserId(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("userId", Long.class);
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

    public String createAccessToken(Long userId, String providerId, String role) {

        return Jwts.builder()
                .claim("tokenType", "access")
                .claim("userId", userId)
                .claim("providerId", providerId)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRED_IN))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String createRefreshToken(Long userId, String providerId) {

        return Jwts.builder()
                .claim("tokenType", "refresh")
                .claim("userId", userId)
                .claim("providerId", providerId)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRED_IN))
                .signWith(secretKey, Jwts.SIG.HS256)
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
            throw new CustomJwtException(ErrorCode.EXPIRED_ACCESS_TOKEN);
        } catch (UnsupportedJwtException e) { // 지원되지 않는 형식
            throw new CustomJwtException(ErrorCode.UNSUPPORTED_TOKEN_TYPE);
        } catch (MalformedJwtException e) { // 구조가 잘못된 토큰
            throw new CustomJwtException(ErrorCode.MALFORMED_TOKEN_TYPE);
        } catch (SignatureException e) { // 서명 위조 (곧 지원 중단)
            throw new CustomJwtException(ErrorCode.INVALID_SIGNATURE_JWT);
        } catch (IllegalArgumentException e) { // 토큰이 비어 있거나 Null
            throw new CustomJwtException(ErrorCode.EMPTY_AUTHORIZATION_HEADER);
        } catch (Exception e) { // 기타 예외 상황
            throw new CustomJwtException(ErrorCode.SECURITY_INVALID_ACCESS_TOKEN);
        }
    }

    public String getUserNameFromToken(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("name", String.class);
    }

    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(ACCESS_HEADER))
                .filter(accessToken -> accessToken.startsWith(BEARER_PREFIX))
                .map(accessToken -> accessToken.replace(BEARER_PREFIX, ""));
    }

    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(REFRESH_HEADER))
                .filter(refreshToken -> refreshToken.startsWith(BEARER_PREFIX))
                .map(refreshToken -> refreshToken.replace(BEARER_PREFIX, ""));
    }
}
