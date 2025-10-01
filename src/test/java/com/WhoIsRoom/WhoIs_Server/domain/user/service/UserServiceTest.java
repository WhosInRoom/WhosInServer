package com.WhoIsRoom.WhoIs_Server.domain.user.service;

import com.WhoIsRoom.WhoIs_Server.domain.club.model.Club;
import com.WhoIsRoom.WhoIs_Server.domain.club.repository.ClubRepository;
import com.WhoIsRoom.WhoIs_Server.domain.member.model.Member;
import com.WhoIsRoom.WhoIs_Server.domain.member.repository.MemberRepository;
import com.WhoIsRoom.WhoIs_Server.domain.user.dto.request.MyPageUpdateRequest;
import com.WhoIsRoom.WhoIs_Server.domain.user.dto.response.MyPageResponse;
import com.WhoIsRoom.WhoIs_Server.domain.user.model.Role;
import com.WhoIsRoom.WhoIs_Server.domain.user.model.User;
import com.WhoIsRoom.WhoIs_Server.domain.user.repository.UserRepository;
import com.WhoIsRoom.WhoIs_Server.domain.user.dto.response.ClubResponse;

import com.WhoIsRoom.WhoIs_Server.global.common.exception.BusinessException;
import com.WhoIsRoom.WhoIs_Server.global.common.response.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.assertj.core.api.Assertions.*;


@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // H2로 대체 금지
class UserServiceTest {

    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private ClubRepository clubRepository;
    @Autowired
    private MemberRepository memberRepository;

    @PersistenceContext
    EntityManager em;

    private User user;
    private Club c1, c2, c3, c4;

    @BeforeEach
    void setUp() {
        // 깨끗한 상태로 시작(FK 제약 있으면 순서 중요)
        memberRepository.deleteAllInBatch();
        clubRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        // ⚠️ User, Club 엔티티의 @NotNull 필드가 있다면 실제 필드 모두 채워줘야 함
        user = userRepository.save(User.builder()
                .nickName("oldNick")
                        .role(Role.MEMBER)
                        .email("oldEmail")
                        .password("oldPassword")
                .build());

        c1 = clubRepository.save(Club.builder().name("C1").clubNumber("1").build());
        c2 = clubRepository.save(Club.builder().name("C2").clubNumber("2").build());
        c3 = clubRepository.save(Club.builder().name("C3").clubNumber("3").build());
        c4 = clubRepository.save(Club.builder().name("C4").clubNumber("4").build());

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("성공: 추가만 (현재 {c1,c2} → 요청 {c1,c2,c3})")
    void addOnly() {
        memberRepository.save(Member.builder().user(user).club(c1).build());
        memberRepository.save(Member.builder().user(user).club(c2).build());
        em.flush(); em.clear();

        MyPageUpdateRequest req = MyPageUpdateRequest.builder()
                .nickName("oldNick")
                .clubList(List.of(c1.getId(), c2.getId(), c3.getId()))
                .build();

        MyPageResponse resp = userService.updateMyPage(user.getId(), req);

        assertThat(resp.getClubList())
                .extracting(ClubResponse::getId)
                .containsExactlyInAnyOrder(c1.getId(), c2.getId(), c3.getId());

        // DB 상태도 확인
        var after = memberRepository.findByUserId(user.getId());
        assertThat(after).extracting(m -> m.getClub().getId())
                .containsExactlyInAnyOrder(c1.getId(), c2.getId(), c3.getId());
    }

    @Test
    @DisplayName("성공: 삭제만 (현재 {c1,c2,c3} → 요청 {c2,c3})")
    void removeOnly() {
        memberRepository.save(Member.builder().user(user).club(c1).build());
        memberRepository.save(Member.builder().user(user).club(c2).build());
        memberRepository.save(Member.builder().user(user).club(c3).build());
        em.flush(); em.clear();

        MyPageUpdateRequest req = MyPageUpdateRequest.builder()
                .nickName("oldNick")
                .clubList(List.of(c2.getId(), c3.getId()))
                .build();

        MyPageResponse resp = userService.updateMyPage(user.getId(), req);

        assertThat(resp.getClubList())
                .extracting(ClubResponse::getId)
                .containsExactlyInAnyOrder(c2.getId(), c3.getId());

        var after = memberRepository.findByUserId(user.getId());
        assertThat(after).extracting(m -> m.getClub().getId())
                .containsExactlyInAnyOrder(c2.getId(), c3.getId());
    }

    @Test
    @DisplayName("성공: 추가+삭제 (현재 {c1,c2,c3} → 요청 {c2,c4})")
    void addAndRemove() {
        memberRepository.save(Member.builder().user(user).club(c1).build());
        memberRepository.save(Member.builder().user(user).club(c2).build());
        memberRepository.save(Member.builder().user(user).club(c3).build());
        em.flush(); em.clear();

        MyPageUpdateRequest req = MyPageUpdateRequest.builder()
                .nickName("oldNick")
                .clubList(List.of(c2.getId(), c4.getId()))
                .build();

        MyPageResponse resp = userService.updateMyPage(user.getId(), req);

        assertThat(resp.getClubList())
                .extracting(ClubResponse::getId)
                .containsExactlyInAnyOrder(c2.getId(), c4.getId());

        var after = memberRepository.findByUserId(user.getId());
        assertThat(after).extracting(m -> m.getClub().getId())
                .containsExactlyInAnyOrder(c2.getId(), c4.getId());
    }

    @Test
    @DisplayName("성공: 전체 탈퇴 (요청 null)")
    void leaveAll() {
        memberRepository.save(Member.builder().user(user).club(c1).build());
        memberRepository.save(Member.builder().user(user).club(c2).build());
        em.flush(); em.clear();

        MyPageUpdateRequest req = MyPageUpdateRequest.builder()
                .nickName("oldNick")
                .clubList(null) // 전체 탈퇴로 간주
                .build();

        MyPageResponse resp = userService.updateMyPage(user.getId(), req);

        assertThat(resp.getClubList()).isEmpty();
        assertThat(memberRepository.findByUserId(user.getId())).isEmpty();
    }

    @Test
    @DisplayName("실패: 존재하지 않는 Club ID 포함 → CLUB_NOT_FOUND")
    void clubNotFound() {
        Long notExistId = 9999L;

        memberRepository.save(Member.builder().user(user).club(c1).build());
        em.flush(); em.clear();

        MyPageUpdateRequest req = MyPageUpdateRequest.builder()
                .nickName("oldNick")
                .clubList(List.of(c1.getId(), notExistId))
                .build();

        assertThatThrownBy(() -> userService.updateMyPage(user.getId(), req))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CLUB_NOT_FOUND);
    }
}
