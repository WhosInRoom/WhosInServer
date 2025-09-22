package com.WhoIsRoom.WhoIs_Server.domain.test.controller;

import com.WhoIsRoom.WhoIs_Server.global.common.exception.BusinessException;
import com.WhoIsRoom.WhoIs_Server.global.common.response.BaseResponse;
import com.WhoIsRoom.WhoIs_Server.global.common.response.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/health-check")
    public BaseResponse<Void> test() {
        log.info("=== Test Controller test 진입 ===");
        return BaseResponse.ok(null);
    }

    @GetMapping("/error-test")
    public BaseResponse<Void> errorTest() {
        log.info("=== Test Controller errorTest 진입 ===");
        throw new BusinessException(ErrorCode.SERVER_ERROR);
    }
}

