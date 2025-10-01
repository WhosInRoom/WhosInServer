package com.WhoIsRoom.WhoIs_Server.domain.auth.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordRequest {
    private String prePassword;
    private String newPassword;
}
