package com.WhoIsRoom.WhoIs_Server.domain.attendance.repository;

import com.WhoIsRoom.WhoIs_Server.domain.attendance.model.Attendance;
import com.WhoIsRoom.WhoIs_Server.domain.club.model.Club;
import com.WhoIsRoom.WhoIs_Server.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByUserAndClubAndCheckOutAtIsNull(User user, Club club);
}
