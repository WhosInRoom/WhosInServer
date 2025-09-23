package com.WhoIsRoom.WhoIs_Server.global.config;

import com.WhoIsRoom.WhoIs_Server.domain.auth.filter.JwtAuthenticationFilter;
import com.WhoIsRoom.WhoIs_Server.domain.auth.handler.exception.CustomAccessDeniedHandler;
import com.WhoIsRoom.WhoIs_Server.domain.auth.handler.exception.CustomAuthenticationEntryPoint;
import com.WhoIsRoom.WhoIs_Server.domain.auth.handler.exception.JwtExceptionHandlerFilter;
import com.WhoIsRoom.WhoIs_Server.domain.auth.handler.success.CustomAuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // Custom한 것들은 Component로 주입하기 (싱글턴 Bean)
    // private final CustomOAuth2UserService customOuth2UserService;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final JwtExceptionHandlerFilter jwtExceptionHandlerFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .cors(cors -> cors.disable())
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable());

        http
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtExceptionHandlerFilter, JwtAuthenticationFilter.class);

        http
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                );

        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/**").permitAll()
                        // 나머지 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                );

        // 일반 ID/비밀번호 로그인 설정
        http
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .loginProcessingUrl("/login-process") // 로그인 폼 제출 처리 URL
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(customAuthenticationSuccessHandler) // 일반 로그인도 동일한 성공 핸들러 사용
                        .permitAll() // 로그인 페이지는 모두 접근 가능
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
}
