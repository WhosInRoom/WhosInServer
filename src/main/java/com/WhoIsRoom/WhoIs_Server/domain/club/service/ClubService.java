package com.WhoIsRoom.WhoIs_Server.domain.club.service;

import com.WhoIsRoom.WhoIs_Server.domain.attendance.model.Attendance;
import com.WhoIsRoom.WhoIs_Server.domain.attendance.repository.AttendanceRepository;
import com.WhoIsRoom.WhoIs_Server.domain.club.model.Club;
import com.WhoIsRoom.WhoIs_Server.domain.club.repository.ClubRepository;
import com.WhoIsRoom.WhoIs_Server.domain.user.model.User;
import com.WhoIsRoom.WhoIs_Server.domain.user.repository.UserRepository;
import com.WhoIsRoom.WhoIs_Server.global.common.exception.BusinessException;
import com.WhoIsRoom.WhoIs_Server.global.common.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClubService {
    private final ClubRepository clubRepository;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;

    @Transactional
    public void checkIn(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        User user = getCurrentUser();

        attendanceRepository.findByUserAndClubAndCheckOutAtIsNull(user, club)
                .ifPresent(a -> { throw new BusinessException(ErrorCode.ALREADY_CHECKED_IN); });

        Attendance attendance = Attendance.builder()
                .user(user)
                .club(club)
                .checkInAt(LocalDateTime.now())
                .build();
        attendanceRepository.save(attendance);
    }

    @Transactional
    public void checkOut(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        User user = getCurrentUser();

        Attendance attendance = attendanceRepository.findByUserAndClubAndCheckOutAtIsNull(user, club)
                .orElseThrow(() -> new BusinessException(ErrorCode.ATTENDANCE_NOT_FOUND));

        attendance.checkOut();
    }

    private User getCurrentUser() {
        String nickname = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByNickName(nickname)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
