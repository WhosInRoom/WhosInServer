package com.WhoIsRoom.WhoIs_Server.global.common.model;

import lombok.Getter;

@Getter
public enum BaseStatus {

    ACTIVE,        // 유효한 데이터
    INACTIVE      // 유효하지 않은 데이터 ( 삭제된 데이터 )
}