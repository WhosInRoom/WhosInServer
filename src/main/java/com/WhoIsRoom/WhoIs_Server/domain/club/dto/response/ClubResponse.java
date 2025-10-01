package com.WhoIsRoom.WhoIs_Server.domain.club.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ClubResponse {
    private Long clubId;
    private String clubName;
}
