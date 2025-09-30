package com.WhoIsRoom.WhoIs_Server.domain.user.repository;

import com.WhoIsRoom.WhoIs_Server.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByNickName(String nickName);
}
