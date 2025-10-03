package com.WhoIsRoom.WhoIs_Server.domain.club.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ClubPresenceResponse {
    private String clubName;
    private List<PresenceResponse> presentMembers;
}
