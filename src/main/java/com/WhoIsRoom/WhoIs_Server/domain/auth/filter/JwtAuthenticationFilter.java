package com.WhoIsRoom.WhoIs_Server.domain.auth.filter;

import com.WhoIsRoom.WhoIs_Server.domain.auth.exception.CustomAuthenticationException;
import com.WhoIsRoom.WhoIs_Server.domain.auth.exception.CustomJwtException;
import com.WhoIsRoom.WhoIs_Server.domain.auth.model.UserPrincipal;
import com.WhoIsRoom.WhoIs_Server.domain.auth.service.JwtService;
import com.WhoIsRoom.WhoIs_Server.domain.auth.util.JwtUtil;
import com.WhoIsRoom.WhoIs_Server.global.common.response.ErrorCode;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.security.sasl.AuthenticationException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final JwtService jwtService;

    // 인증을 안해도 되니 토큰이 필요없는 URL들 (에러: 로그인이 필요합니다)
    public final static List<String> PASS_URIS = Arrays.asList(
            "/api/users/signup",
            "/api/auth/**"
    );

    private static final AntPathMatcher ANT = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if(isPassUri(request.getRequestURI())) {
            log.info("JWT Filter Passed (pass uri) : {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        // 엑세스 토큰이 없으면 Authentication도 없음 -> EntryPoint (401)
        log.info("Request URI: {}", request.getRequestURI()); // 요청 URI 로깅
        String accessToken = jwtUtil.extractAccessToken(request)
                .orElseThrow(() -> new CustomAuthenticationException(ErrorCode.SECURITY_UNAUTHORIZED));

        // 토큰 유효성 검사
        jwtUtil.validateToken(accessToken);

        // 토큰 타입 검사
        if(!"access".equals(jwtUtil.getTokenType(accessToken))) {
            throw new CustomJwtException(ErrorCode.INVALID_TOKEN_TYPE);
        }

        // 로그아웃 체크
        jwtService.checkLogout(accessToken);

        // 권한 리스트 생성
        List<GrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority(jwtUtil.getRole(accessToken)));
        log.info("Granted Authorities : {}", authorities);
        UserPrincipal principal = new UserPrincipal(
                jwtUtil.getUserId(accessToken),
                jwtUtil.getName(accessToken),
                jwtUtil.getEmail(accessToken),
                null, // 패스워드는 필요 없음
                jwtUtil.getProviderId(accessToken),
                authorities
        );
        log.info("UserPrincipal.userId: {}", principal.getUserId());
        log.info("UserPrincipal.name: {}", principal.getUsername());
        log.info("UserPrincipal.email: {}", principal.getEmail());
        log.info("UserPrincipal.providerId: {}", principal.getProviderId());
        log.info("UserPrincipal.role: {}", principal.getAuthorities().stream().findFirst().get().toString());

        Authentication authToken = null;
        if ("localhost".equals(principal.getProviderId())) {
            // 폼 로그인(자체 회원)
            authToken = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        }
//        else {
//            // 소셜 로그인
//      authToken = new OAuth2AuthenticationToken(principal, authorities, loginProvider);
//        }
        log.info("Authentication set in SecurityContext: {}", SecurityContextHolder.getContext().getAuthentication());
        log.info("Authorities in SecurityContext: {}", authToken.getAuthorities());

        log.info("JWT Filter Success : {}", request.getRequestURI());
        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);
    }

    private boolean isPassUri(String uri) {
        return PASS_URIS.stream().anyMatch(pattern -> ANT.match(pattern, uri));
    }
}
