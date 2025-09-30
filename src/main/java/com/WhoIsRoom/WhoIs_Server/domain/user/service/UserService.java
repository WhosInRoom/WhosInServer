package com.WhoIsRoom.WhoIs_Server.domain.user.service;

import com.WhoIsRoom.WhoIs_Server.domain.auth.dto.request.MailRequest;
import com.WhoIsRoom.WhoIs_Server.domain.auth.dto.request.PasswordRequest;
import com.WhoIsRoom.WhoIs_Server.domain.auth.service.MailService;
import com.WhoIsRoom.WhoIs_Server.domain.club.model.Club;
import com.WhoIsRoom.WhoIs_Server.domain.member.model.Member;
import com.WhoIsRoom.WhoIs_Server.domain.member.repository.MemberRepository;
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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final MemberRepository memberRepository;

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
}
