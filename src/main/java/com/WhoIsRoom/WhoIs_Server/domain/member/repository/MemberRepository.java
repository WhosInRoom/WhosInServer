package com.WhoIsRoom.WhoIs_Server.domain.member.repository;

import com.WhoIsRoom.WhoIs_Server.domain.member.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findByUserId(Long userId);
}
