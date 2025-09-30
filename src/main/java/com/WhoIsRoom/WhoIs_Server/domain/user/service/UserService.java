package com.WhoIsRoom.WhoIs_Server.domain.user.service;

import com.WhoIsRoom.WhoIs_Server.domain.auth.dto.request.MailRequest;
import com.WhoIsRoom.WhoIs_Server.domain.auth.dto.request.PasswordRequest;
import com.WhoIsRoom.WhoIs_Server.domain.auth.service.MailService;
import com.WhoIsRoom.WhoIs_Server.domain.club.model.Club;
import com.WhoIsRoom.WhoIs_Server.domain.club.repository.ClubRepository;
import com.WhoIsRoom.WhoIs_Server.domain.member.model.Member;
import com.WhoIsRoom.WhoIs_Server.domain.member.repository.MemberRepository;
import com.WhoIsRoom.WhoIs_Server.domain.user.dto.request.MyPageUpdateRequest;
import com.WhoIsRoom.WhoIs_Server.domain.user.dto.request.SignupRequest;
import com.WhoIsRoom.WhoIs_Server.domain.user.dto.response.MyPageResponse;
import com.WhoIsRoom.WhoIs_Server.domain.user.model.Role;
import com.WhoIsRoom.WhoIs_Server.domain.user.model.User;
import com.WhoIsRoom.WhoIs_Server.domain.user.repository.UserRepository;
import com.WhoIsRoom.WhoIs_Server.global.common.exception.BusinessException;
import com.WhoIsRoom.WhoIs_Server.global.common.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final MemberRepository memberRepository;
    private final ClubRepository clubRepository;

    @Transactional
    public void signUp(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.USER_DUPLICATE_EMAIL);
        }
        if (userRepository.existsByNickName(request.getNickName())) {
            throw new BusinessException(ErrorCode.USER_DUPLICATE_NICKNAME);
        }
        if (!"VERIFIED".equals(mailService.getStoredCode(request.getEmail()))){
            throw new BusinessException(ErrorCode.AUTHCODE_UNAUTHORIZED);
        }

        User user = User.builder()
                .email(request.getEmail())
                .nickName(request.getNickName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.MEMBER)
                .build();
        userRepository.save(user);
    }

    @Transactional
    public void sendNewPassword(MailRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        String newPassword = mailService.sendPasswordMail(request);
        user.setPassword(passwordEncoder.encode(newPassword));
    }

    @Transactional
    public void updateMyPassword(Long userId, PasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPrePassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    }

    @Transactional(readOnly = true)
    public MyPageResponse getMyPage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<Member> memberList = memberRepository.findByUserId(userId);
        return MyPageResponse.from(user.getNickName(), memberList);
    }

    @Transactional
    public MyPageResponse updateMyPage(Long userId, MyPageUpdateRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        updateUserNickName(user, request.getNickName());

        updateUserClubs(user, request.getClubList());

        List<Member> updatedMemberList = memberRepository.findByUserId(userId);
        return MyPageResponse.from(user.getNickName(), updatedMemberList);
    }

    private void updateUserNickName(User user, String newNickName) {

        // 변경 사항이 없으면 아무것도 하지 않음 (최적화)
        if (user.getNickName().equals(newNickName)) {
            return;
        }

        // 닉네임 중복 검사 (자기 자신은 제외되므로 안전함)
        if (userRepository.existsByNickName(newNickName)) {
            throw new BusinessException(ErrorCode.USER_DUPLICATE_NICKNAME);
        }

        user.setNickName(newNickName);
    }

    private void updateUserClubs(User user, List<Long> newClubIdList) {

        // null이면 빈 리스트로 간주 => 모두 탈퇴 처리
        Set<Long> requested = newClubIdList == null ? Set.of()
                : newClubIdList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new)); // 순서 유지 필요시

        Long userId = user.getId();

        // 현재 가입된 clubId 목록
        Set<Long> current = new LinkedHashSet<>(memberRepository.findClubIdsByUserId(userId));

        // 계산: 추가/삭제 집합
        Set<Long> toAdd = new LinkedHashSet<>(requested);
        toAdd.removeAll(current);

        Set<Long> toRemove = new LinkedHashSet<>(current);
        toRemove.removeAll(requested);

        // 삭제 먼저 (없으면 no-op)
        if (!toRemove.isEmpty()) {
            memberRepository.deleteByUserIdAndClubIdIn(userId, toRemove);
        }

        // 추가할 Club의 존재성 검증
        if (!toAdd.isEmpty()) {
            List<Club> clubs = clubRepository.findAllById(toAdd);

            if (clubs.size() != toAdd.size()) {
                // 어떤 ID는 존재X
                throw new BusinessException(ErrorCode.CLUB_NOT_FOUND);
            }

            // Member 엔티티 생성
            List<Member> newMembers = clubs.stream()
                    .map(club -> Member.builder()
                            .user(user)
                            .club(club)
                            .build())
                    .toList();

            // 저장 (유니크 제약 (user_id, club_id) 있어도 toAdd는 중복이 아님)
            memberRepository.saveAll(newMembers);
        }
    }
}
