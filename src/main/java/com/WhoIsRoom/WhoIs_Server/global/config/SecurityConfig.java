package com.WhoIsRoom.WhoIs_Server.global.config;

import com.WhoIsRoom.WhoIs_Server.domain.auth.filter.CustomLoginFilter;
import com.WhoIsRoom.WhoIs_Server.domain.auth.filter.JwtAuthenticationFilter;
import com.WhoIsRoom.WhoIs_Server.domain.auth.handler.exception.CustomAccessDeniedHandler;
import com.WhoIsRoom.WhoIs_Server.domain.auth.handler.exception.CustomAuthenticationEntryPoint;
import com.WhoIsRoom.WhoIs_Server.domain.auth.handler.exception.CustomJsonAuthenticationFailureHandler;
import com.WhoIsRoom.WhoIs_Server.domain.auth.handler.exception.JwtExceptionHandlerFilter;
import com.WhoIsRoom.WhoIs_Server.domain.auth.handler.success.CustomAuthenticationSuccessHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // Custom한 것들은 Component로 주입하기 (싱글턴 Bean)
    // private final CustomOAuth2UserService customOuth2UserService;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final CustomJsonAuthenticationFailureHandler customJsonAuthenticationFailureHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final JwtExceptionHandlerFilter jwtExceptionHandlerFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;
    private final AuthenticationConfiguration configuration;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomLoginFilter customLoginFilter) throws Exception {
        http
                // 기본 옵션, 폼 로그인 비활성화
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .httpBasic(b -> b.disable())
                .formLogin(fl -> fl.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http
                .addFilterBefore(jwtExceptionHandlerFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(customLoginFilter, UsernamePasswordAuthenticationFilter.class);

        http
                // 시큐리티 표준 예외 핸들러 (401/403)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                );

        http
                // 인가 규칙
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/**").permitAll()
                        // 나머지 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                );

        // OAuth2 소셜 로그인 설정
//        http
//                .oauth2Login(oauth2Login -> oauth2Login
//                        .loginPage("/login")
//                        .userInfoEndpoint(userInfo -> userInfo
//                                .userService(customOAuth2UserService) // OAuth2 사용자 정보 처리
//                        )
//                        .successHandler(customAuthenticationSuccessHandler) // 소셜 로그인도 동일한 성공 핸들러 사용
//                );
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CustomLoginFilter customLoginFilter() throws Exception {
        return new CustomLoginFilter(
                authenticationManager(),
                objectMapper,
                customAuthenticationSuccessHandler,
                customJsonAuthenticationFailureHandler
        );
    }
}
