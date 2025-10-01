package com.WhoIsRoom.WhoIs_Server.domain.member.repository;

import com.WhoIsRoom.WhoIs_Server.domain.member.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findByUserId(Long userId);
    // 현재 유저가 속한 clubId 목록만 빠르게 가져오기
    @Query("select m.club.id from Member m where m.user.id = :userId")
    List<Long> findClubIdsByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Member m where m.user.id = :userId and m.club.id in :clubIds")
    void deleteByUserIdAndClubIdIn(@Param("userId") Long userId, @Param("clubIds") Collection<Long> clubIds);
}
