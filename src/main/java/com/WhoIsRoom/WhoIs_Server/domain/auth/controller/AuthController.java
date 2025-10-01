package com.WhoIsRoom.WhoIs_Server.domain.auth.controller;

import com.WhoIsRoom.WhoIs_Server.domain.auth.dto.request.CodeCheckRequest;
import com.WhoIsRoom.WhoIs_Server.domain.auth.dto.request.MailRequest;
import com.WhoIsRoom.WhoIs_Server.domain.auth.dto.request.PasswordRequest;
import com.WhoIsRoom.WhoIs_Server.domain.auth.dto.request.RefreshTokenRequest;
import com.WhoIsRoom.WhoIs_Server.domain.auth.dto.response.ReissueResponse;
import com.WhoIsRoom.WhoIs_Server.domain.auth.service.JwtService;
import com.WhoIsRoom.WhoIs_Server.domain.auth.service.MailService;
import com.WhoIsRoom.WhoIs_Server.domain.user.service.UserService;
import com.WhoIsRoom.WhoIs_Server.global.common.resolver.CurrentUserId;
import com.WhoIsRoom.WhoIs_Server.global.common.response.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;
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
    public BaseResponse<Void> logout(HttpServletRequest request,
                                     @RequestBody RefreshTokenRequest tokenRequest){
        jwtService.logout(request, tokenRequest);
        return BaseResponse.ok(null);
    }

    @PostMapping("/reissue")
    public BaseResponse<ReissueResponse> reissueTokens(@RequestBody RefreshTokenRequest tokenRequest) {
        ReissueResponse response = jwtService.reissueTokens(tokenRequest);
        return BaseResponse.ok(response);
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
        userService.sendNewPassword(request);
        return BaseResponse.ok(null);
    }
}
