package com.WhoIsRoom.WhoIs_Server.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReissueResponse {
    private String accessToken;
    private String refreshToken;
}
