package com.WhoIsRoom.WhoIs_Server.domain.user.dto.response;

import com.WhoIsRoom.WhoIs_Server.domain.member.model.Member;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyPageResponse {
    private String nickname;
    private List<ClubResponse> clubList;

    public static MyPageResponse from(String nickname, List<Member> memberList) {

        List<ClubResponse> clubList = memberList.stream()
                .map(Member::getClub)
                .distinct()
                .map(ClubResponse::from)
                .toList();

        return MyPageResponse.builder()
                .nickname(nickname)
                .clubList(clubList)
                .build();
    }
}
