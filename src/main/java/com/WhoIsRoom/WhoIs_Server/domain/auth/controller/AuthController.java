package com.WhoIsRoom.WhoIs_Server.domain.auth.controller;

import com.WhoIsRoom.WhoIs_Server.domain.auth.service.JwtService;
import com.WhoIsRoom.WhoIs_Server.global.common.response.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtService jwtService;

    @PostMapping("/logout")
    public BaseResponse<Void> logout(HttpServletRequest request){
        jwtService.logout(request);
        return BaseResponse.ok(null);
    }

    @PostMapping("/reissue")
    public BaseResponse<Void> reissueTokens(HttpServletRequest request, HttpServletResponse response) {
        jwtService.reissueTokens(request, response);
        return BaseResponse.ok(null);
    }
}
