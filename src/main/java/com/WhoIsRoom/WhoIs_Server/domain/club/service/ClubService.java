package com.WhoIsRoom.WhoIs_Server.domain.club.service;

import com.WhoIsRoom.WhoIs_Server.domain.club.dto.response.ClubPresenceResponse;
import com.WhoIsRoom.WhoIs_Server.domain.club.dto.response.ClubResponse;
import com.WhoIsRoom.WhoIs_Server.domain.club.dto.response.MyClubsResponse;
import com.WhoIsRoom.WhoIs_Server.domain.club.dto.response.PresenceResponse;
import com.WhoIsRoom.WhoIs_Server.domain.club.model.Club;
import com.WhoIsRoom.WhoIs_Server.domain.club.repository.ClubRepository;
import com.WhoIsRoom.WhoIs_Server.domain.member.model.Member;
import com.WhoIsRoom.WhoIs_Server.domain.member.repository.MemberRepository;
import com.WhoIsRoom.WhoIs_Server.domain.user.model.User;
import com.WhoIsRoom.WhoIs_Server.domain.user.repository.UserRepository;
import com.WhoIsRoom.WhoIs_Server.global.common.exception.BusinessException;
import com.WhoIsRoom.WhoIs_Server.global.common.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClubService {
    private final ClubRepository clubRepository;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void checkIn(Long clubId) {
        User user = getCurrentUser();

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLUB_NOT_FOUND));

        Member member = memberRepository.findByUserAndClub(user, club)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (Boolean.TRUE.equals(member.getIsExist())) {
            throw new BusinessException(ErrorCode.ALREADY_CHECKED_IN);
        }

        member.setExist(true);
    }

    @Transactional
    public void checkOut(Long clubId) {
        User user = getCurrentUser();

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLUB_NOT_FOUND));

        Member member = memberRepository.findByUserAndClub(user, club)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (Boolean.FALSE.equals(member.getIsExist())) {
            throw new BusinessException(ErrorCode.ATTENDANCE_NOT_FOUND);
        }

        member.setExist(false);
    }

    private User getCurrentUser() {
        String nickname = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByNickName(nickname)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public void joinClub(Long clubId) {
        User user = getCurrentUser();

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLUB_NOT_FOUND));

        memberRepository.findByUserAndClub(user, club).ifPresent(member -> {
            throw new BusinessException(ErrorCode.ALREADY_MEMBER);
        });

        Member member = Member.builder()
                .user(user)
                .club(club)
                .isExist(false)
                .build();

        memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public ClubResponse getClubByClubNumber(String clubNumber) {
        Club club = clubRepository.findByClubNumber(clubNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLUB_NOT_FOUND));

        return new ClubResponse(club.getId(), club.getName());
    }

    @Transactional(readOnly = true)
    public MyClubsResponse getMyClubs() {
        User user = getCurrentUser();

        List<Member> members = memberRepository.findByUser(user);

        List<ClubResponse> userClubs = members.stream()
                .map(member -> ClubResponse.builder()
                        .clubId(member.getClub().getId())
                        .clubName(member.getClub().getName())
                        .build())
                .toList();

        return MyClubsResponse.builder()
                .userClubs(userClubs)
                .build();
    }

    @Transactional(readOnly = true)
    public ClubPresenceResponse getClubPresence(Long clubId) {
        User user = getCurrentUser();

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLUB_NOT_FOUND));

        memberRepository.findByUserAndClub(user, club)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        List<Member> presentMembers = memberRepository.findAllByClubAndIsExistTrue(club);

        List<PresenceResponse> response = presentMembers.stream()
                .map(member -> new PresenceResponse(
                        member.getUser().getNickName(),
                        member.getUser().getId().equals(user.getId())
                ))
                .toList();

        return new ClubPresenceResponse(club.getName(), response);
    }
}
