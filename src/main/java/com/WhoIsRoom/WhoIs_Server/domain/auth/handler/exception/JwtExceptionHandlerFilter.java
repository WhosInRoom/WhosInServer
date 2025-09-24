package com.WhoIsRoom.WhoIs_Server.domain.auth.handler.exception;

import com.WhoIsRoom.WhoIs_Server.domain.auth.exception.CustomJwtException;
import com.WhoIsRoom.WhoIs_Server.global.common.exception.BusinessException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.WhoIsRoom.WhoIs_Server.domain.auth.util.SecurityErrorResponseUtil.setErrorResponse;

@Slf4j
@Component
public class JwtExceptionHandlerFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("=== JwtExceptionHandlerFilter 진입 ===");

        try {
            filterChain.doFilter(request, response);
        } catch (CustomJwtException e) {
            setErrorResponse(response, e.getErrorCode());
        } catch (BusinessException e) {
            setErrorResponse(response, e.getErrorCode());
        }
    }
}
