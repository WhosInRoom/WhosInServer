package com.WhoIsRoom.WhoIs_Server.domain.member.repository;

import com.WhoIsRoom.WhoIs_Server.domain.club.model.Club;
import com.WhoIsRoom.WhoIs_Server.domain.member.model.Member;
import com.WhoIsRoom.WhoIs_Server.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUserAndClub(User user, Club club);
}
