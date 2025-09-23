package com.WhoIsRoom.WhoIs_Server.domain.auth.filter;

import com.WhoIsRoom.WhoIs_Server.domain.auth.model.UserPrincipal;
import com.WhoIsRoom.WhoIs_Server.domain.auth.service.JwtService;
import com.WhoIsRoom.WhoIs_Server.domain.auth.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final JwtService jwtService;

    // 인증을 안해도 되니 토큰이 필요없는 URL들 (에러: 로그인이 필요합니다)
    public final static List<String> PASS_URIS = Arrays.asList(
            "/api/login",
            "/api/logout",
            "/api/signup"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if(isPassUris(request.getRequestURI())) {
            log.info("JWT Filter Passed (pass uri) : {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        log.info("Request URI: {}", request.getRequestURI()); // 요청 URI 로깅
        String accessToken = jwtUtil.resolveAccessToken(request);

        // 엑세스 토큰이 없으면 Authentication도 없음 -> EntryPoint (401)
        if(accessToken == null) {
            log.info("JWT Filter Pass (accessToken is null) : {}", request.getRequestURI());
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 유효성 검사
        jwtUtil.validateToken(accessToken);

        // 토큰 타입 검사
        if(!"access".equals(jwtUtil.getTokenType(accessToken))) {
            throw new JwtException(BaseResponseStatus.INVALID_TOKEN_TYPE);
        }

        jwtService.checkLogout(request);

        // 권한 리스트 생성
        List<GrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority(jwtUtil.getRole(accessToken)));
        log.info("Granted Authorities : {}", authorities);
        UserPrincipal principal = new UserPrincipal(
                jwtUtil.getMemberId(accessToken),
                jwtUtil.getEmail(accessToken),
                null, // 패스워드는 필요 없음
                authorities
        );
        log.info("UserPrincipal created: {}", principal); // 생성된 사용자 정보 로깅
        log.info("UserPrincipal.providerId: {}", principal.getProviderId());
        log.info("UserPrincipal.role: {}", principal.getAuthorities().stream().findFirst().get().toString());
        log.info("UserPrincipal.memberId: {}", principal.getUserId());

        Authentication authToken = null;
        if ("localhost".equals(loginProvider)) {
            // 폼 로그인(자체 회원)
            authToken = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        }
//        else {
//            // 소셜 로그인
//      authToken = new OAuth2AuthenticationToken(principal, authorities, loginProvider);
//        }
        log.info("Authentication set in SecurityContext: {}", SecurityContextHolder.getContext().getAuthentication()); // SecurityContext 설정 확인 로깅
        log.info("Authorities in SecurityContext: {}", authToken.getAuthorities());

        log.info("JWT Filter Success : {}", request.getRequestURI());
        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);
    }

    private boolean isPassUris(String uri) {
        return PASS_URIS.contains(uri);
    }
}
