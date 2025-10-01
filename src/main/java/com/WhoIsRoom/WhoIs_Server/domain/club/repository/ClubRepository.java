package com.WhoIsRoom.WhoIs_Server.domain.club.repository;

import com.WhoIsRoom.WhoIs_Server.domain.club.model.Club;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClubRepository extends JpaRepository<Club, Long> {
    Optional<Club> findByClubNumber(String clubNumber);
}
