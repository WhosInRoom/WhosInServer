package com.WhoIsRoom.WhoIs_Server.domain.user.controller;

import com.WhoIsRoom.WhoIs_Server.global.common.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public BaseResponse<Void> signUp(User user) {
        userService.signUp();
        return BaseResponse.ok(null);
    }
}
