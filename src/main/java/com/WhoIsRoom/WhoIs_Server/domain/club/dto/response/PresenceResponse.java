package com.WhoIsRoom.WhoIs_Server.domain.club.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PresenceResponse {
    private String userName;

    @JsonProperty("isMe")
    private boolean me;
}
