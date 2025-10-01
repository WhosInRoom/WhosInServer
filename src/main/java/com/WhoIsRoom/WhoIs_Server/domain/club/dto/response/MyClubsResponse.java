package com.WhoIsRoom.WhoIs_Server.domain.club.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class MyClubsResponse {
    private List<ClubResponse> userClubs;
}
