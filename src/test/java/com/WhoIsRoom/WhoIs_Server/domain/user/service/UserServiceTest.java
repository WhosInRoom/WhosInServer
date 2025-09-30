package com.WhoIsRoom.WhoIs_Server.domain.user.service;

import com.WhoIsRoom.WhoIs_Server.domain.club.model.Club;
import com.WhoIsRoom.WhoIs_Server.domain.club.repository.ClubRepository;
import com.WhoIsRoom.WhoIs_Server.domain.member.model.Member;
import com.WhoIsRoom.WhoIs_Server.domain.member.repository.MemberRepository;
import com.WhoIsRoom.WhoIs_Server.domain.user.dto.request.MyPageUpdateRequest;
import com.WhoIsRoom.WhoIs_Server.domain.user.dto.response.MyPageResponse;
import com.WhoIsRoom.WhoIs_Server.domain.user.model.User;
import com.WhoIsRoom.WhoIs_Server.domain.user.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private ClubRepository clubRepository;
    @Mock private MemberRepository memberRepository;

    @InjectMocks
    private UserService userService;

    private User user;
    private List<Club> clubs;

    @BeforeEach
    void setUp() {
        System.out.println("\n[TEST] ========== setUp ==========");
        user = User.builder()
                .nickName("조익성")
                .email("konkuk@gmail.com")
                .password("1234")
                .build();
        user.setId(1L);

        Club club1 = Club.builder().name("메이커스팜").build();       club1.setId(1L);
        Club club2 = Club.builder().name("목방").build();           club2.setId(2L);
        Club club3 = Club.builder().name("건대교지편집위원회").build(); club3.setId(3L);
        Club club4 = Club.builder().name("국어국문학과").build();     club4.setId(4L);

        clubs = List.of(club1, club2, club3, club4);

        System.out.println("[TEST] userId=" + user.getId() + ", nick=" + user.getNickName());
        System.out.println("[TEST] clubs=" + clubs.stream()
                .map(c -> c.getId() + ":" + c.getName()).toList());
        System.out.println("[TEST] =============================\n");
    }

    @Test
    @DisplayName("닉네임과 클럽 목록을 업데이트하고 응답 DTO를 반환한다")
    void updateMyPage_success() {
        Long userId = user.getId();

        MyPageUpdateRequest request = MyPageUpdateRequest.builder()
                .nickName("조익성")
                .clubList(List.of(1L, 2L, 3L, 4L))
                .build();

        // --- 스텁 + 로그 ---
        when(userRepository.findById(userId))
                .thenAnswer(inv -> {
                    System.out.println("[TEST] userRepository.findById(" + userId + ")");
                    return Optional.of(user);
                });

        when(memberRepository.findClubIdsByUserId(userId))
                .thenAnswer(inv -> {
                    System.out.println("[TEST] memberRepository.findClubIdsByUserId(" + userId + ") -> [2]");
                    return List.of(2L);
                });

        when(clubRepository.findAllById(ArgumentMatchers.<Long>anyIterable()))
                .thenAnswer(invocation -> {
                    Iterable<Long> ids = invocation.getArgument(0);
                    List<Long> idList = new ArrayList<>();
                    ids.forEach(idList::add);
                    System.out.println("[TEST] clubRepository.findAllById called with ids=" + idList);
                    var result = clubs.stream()
                            .filter(c -> idList.contains(c.getId()))
                            .collect(Collectors.toList());
                    System.out.println("[TEST] clubRepository.findAllById returns ids=" +
                            result.stream().map(Club::getId).toList());
                    return result;
                });

        when(memberRepository.saveAll(anyCollection()))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    var c = (java.util.Collection<Member>) invocation.getArgument(0);
                    System.out.println("[TEST] memberRepository.saveAll called size=" + c.size() +
                            ", clubIds=" + c.stream().map(m -> m.getClub().getId()).toList());
                    return new ArrayList<>(c);
                });

        when(memberRepository.findByUserId(userId))
                .thenAnswer(inv -> {
                    System.out.println("[TEST] memberRepository.findByUserId(" + userId + ")");
                    var list = clubs.stream()
                            .map(c -> Member.builder().user(user).club(c).build())
                            .collect(Collectors.toList());
                    System.out.println("[TEST] memberRepository.findByUserId returns clubIds=" +
                            list.stream().map(m -> m.getClub().getId()).toList());
                    return list;
                });

        // --- 실행 ---
        System.out.println("\n[TEST] ===== call userService.updateMyPage =====");
        MyPageResponse response = userService.updateMyPage(userId, request);
        System.out.println("[TEST] ===== returned MyPageResponse =====");
        System.out.println("[TEST] resp.nick=" + response.getNickName());
        System.out.println("[TEST] resp.clubs=" + response.getClubList().stream()
                .map(c -> c.getId() + ":" + c.getName()).toList());
        System.out.println("[TEST] ===================================\n");

        // --- 검증 ---
        assertThat(response.getNickName()).isEqualTo("조익성");
        assertThat(response.getClubList()).hasSize(4);
        assertThat(response.getClubList())
                .extracting("name")
                .containsExactlyInAnyOrder("메이커스팜", "목방", "건대교지편집위원회", "국어국문학과");
    }
}
