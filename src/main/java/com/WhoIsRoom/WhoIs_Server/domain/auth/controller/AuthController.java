package com.WhoIsRoom.WhoIs_Server.domain.auth.controller;

import com.WhoIsRoom.WhoIs_Server.domain.auth.dto.request.CodeCheckRequest;
import com.WhoIsRoom.WhoIs_Server.domain.auth.dto.request.MailRequest;
import com.WhoIsRoom.WhoIs_Server.domain.auth.service.JwtService;
import com.WhoIsRoom.WhoIs_Server.domain.auth.service.MailService;
import com.WhoIsRoom.WhoIs_Server.domain.user.service.UserService;
import com.WhoIsRoom.WhoIs_Server.global.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtService jwtService;
    private final MailService mailService;
    private final UserService userService;

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

    @PostMapping("/email/send")
    public BaseResponse<Void> sendAuthCodeMail(@RequestBody MailRequest request) {
        mailService.sendMail(request);
        return BaseResponse.ok(null);
    }

    @PostMapping("/email/validation")
    public BaseResponse<Void> checkAuthCode(@RequestBody CodeCheckRequest request) {
        mailService.checkAuthCode(request);
        return BaseResponse.ok(null);
    }

    @PostMapping("/email/find-password")
    public BaseResponse<Void> findPassword(@RequestBody MailRequest request) {
        userService.updateMyPassword(request);
        return BaseResponse.ok(null);
    }
}
