package com.WhoIsRoom.WhoIs_Server.domain.club.controller;

import com.WhoIsRoom.WhoIs_Server.domain.club.dto.response.ClubPresenceResponse;
import com.WhoIsRoom.WhoIs_Server.domain.club.dto.response.ClubResponse;
import com.WhoIsRoom.WhoIs_Server.domain.club.dto.response.MyClubsResponse;
import com.WhoIsRoom.WhoIs_Server.domain.club.service.ClubService;
import com.WhoIsRoom.WhoIs_Server.global.common.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clubs")
public class ClubController {

    private final ClubService clubService;

    @PostMapping("/{clubId}/check-in")
    public BaseResponse<Void> checkIn(@PathVariable final Long clubId) {
        clubService.checkIn(clubId);
        return BaseResponse.ok(null);
    }

    @DeleteMapping("/{clubId}/check-out")
    public BaseResponse<Void> checkOut(@PathVariable final Long clubId) {
        clubService.checkOut(clubId);
        return BaseResponse.ok(null);
    }

    @PostMapping("/{clubId}")
    public BaseResponse<Void> joinClub(@PathVariable final Long clubId) {
        clubService.joinClub(clubId);
        return BaseResponse.ok(null);
    }

    @GetMapping
    public BaseResponse<ClubResponse> getClubByClubNumber(@RequestParam String clubNumber) {
        ClubResponse response = clubService.getClubByClubNumber(clubNumber);
        return BaseResponse.ok(response);
    }

    @GetMapping("/my")
    public BaseResponse<MyClubsResponse> getMyClubs() {
        MyClubsResponse response = clubService.getMyClubs();
        return BaseResponse.ok(response);
    }

    @GetMapping("/{clubId}/presences")
    public BaseResponse<ClubPresenceResponse> getClubPresence(@PathVariable final Long clubId) {
        ClubPresenceResponse response = clubService.getClubPresence(clubId);
        return BaseResponse.ok(response);
    }
}
