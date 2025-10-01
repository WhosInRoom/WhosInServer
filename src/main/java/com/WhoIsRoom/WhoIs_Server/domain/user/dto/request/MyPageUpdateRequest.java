package com.WhoIsRoom.WhoIs_Server.domain.user.dto.request;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MyPageUpdateRequest {
    String nickName;
    List<Long> clubList;
}
