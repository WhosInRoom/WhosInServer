package com.WhoIsRoom.WhoIs_Server.domain.user.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MyPageUpdateRequest {
    String nickName;
    List<Long> clubList;
}
