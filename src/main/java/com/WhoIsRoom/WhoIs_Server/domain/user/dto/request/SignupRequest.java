package com.WhoIsRoom.WhoIs_Server.domain.user.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignupRequest {
    private String email;
    private String nickName;
    private String password;
}
