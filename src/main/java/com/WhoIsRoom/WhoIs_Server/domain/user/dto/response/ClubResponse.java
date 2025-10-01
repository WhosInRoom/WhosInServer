package com.WhoIsRoom.WhoIs_Server.domain.user.dto.response;

import com.WhoIsRoom.WhoIs_Server.domain.club.model.Club;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClubResponse {
    private Long id;
    private String name;

    public static ClubResponse from(Club club) {
        return ClubResponse.builder()
                .id(club.getId())
                .name(club.getName())
                .build();
    }


}
