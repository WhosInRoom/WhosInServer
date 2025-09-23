package com.WhoIsRoom.WhoIs_Server.domain.user.model;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Optional;

@Getter
public enum Role {

    MEMBER("MEMBER"),
    ADMIN("ADMIN");

    Role(String value) {
        this.value = value;
        this.role = PREFIX + value;
    }

    private static final String PREFIX = "ROLE_";
    private final String value;
    private final String role;

    // 파싱된 값에 맞는 Role을 반환하는 메서드
    public static Optional<Role> fromRole(String roleString) {
        if (roleString != null && roleString.startsWith(PREFIX)) {
            String roleValue = roleString.substring(PREFIX.length());
            for (Role r : values()) {
                if (r.value.equalsIgnoreCase(roleValue)) return Optional.of(r);
            }
        }
        return Optional.empty();
    }

    // Role을 권한으로 변환하는 메서드
    public GrantedAuthority toAuthority() {
        return new SimpleGrantedAuthority(PREFIX + this.value);
    }
}
