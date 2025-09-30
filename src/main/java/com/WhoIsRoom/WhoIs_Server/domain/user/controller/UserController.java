package com.WhoIsRoom.WhoIs_Server.domain.user.controller;

import com.WhoIsRoom.WhoIs_Server.domain.auth.dto.request.PasswordRequest;
import com.WhoIsRoom.WhoIs_Server.domain.user.dto.request.MyPageUpdateRequest;
import com.WhoIsRoom.WhoIs_Server.domain.user.dto.request.SignupRequest;
import com.WhoIsRoom.WhoIs_Server.domain.user.dto.response.MyPageResponse;
import com.WhoIsRoom.WhoIs_Server.domain.user.service.UserService;
import com.WhoIsRoom.WhoIs_Server.global.common.resolver.CurrentUserId;
import com.WhoIsRoom.WhoIs_Server.global.common.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public BaseResponse<Void> signUp(@RequestBody SignupRequest request) {
        userService.signUp(request);
        return BaseResponse.ok(null);
    }

    @GetMapping("/myPage")
    public BaseResponse<MyPageResponse> getMyPage(@CurrentUserId Long userId) {
        MyPageResponse response = userService.getMyPage(userId);
        return BaseResponse.ok(response);
    }

    @PatchMapping("/myPage/update")
    public BaseResponse<MyPageResponse> updateMyPage(@CurrentUserId Long userId,
                                                     @RequestBody MyPageUpdateRequest request) {
        MyPageResponse response = userService.updateMyPage(userId, request);
        return BaseResponse.ok(response);
    }

    @PatchMapping("/password")
    public BaseResponse<Void> updatePassword(@CurrentUserId Long userId,
                                             @RequestBody PasswordRequest request) {
        userService.updateMyPassword(userId, request);
        return BaseResponse.ok(null);
    }
}
