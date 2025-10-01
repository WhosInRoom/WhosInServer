package com.WhoIsRoom.WhoIs_Server.domain.auth.service;

import com.WhoIsRoom.WhoIs_Server.domain.auth.dto.request.CodeCheckRequest;
import com.WhoIsRoom.WhoIs_Server.domain.auth.dto.request.MailRequest;
import com.WhoIsRoom.WhoIs_Server.domain.auth.exception.CustomAuthenticationException;
import com.WhoIsRoom.WhoIs_Server.domain.user.repository.UserRepository;
import com.WhoIsRoom.WhoIs_Server.domain.user.service.UserService;
import com.WhoIsRoom.WhoIs_Server.global.common.exception.BusinessException;
import com.WhoIsRoom.WhoIs_Server.global.common.redis.RedisService;
import com.WhoIsRoom.WhoIs_Server.global.common.response.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.Duration;
import java.util.Random;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class MailService {

    private static final long VERIFICATION_CODE_EXPIRY_MINUTES = 5;

    private static final long VERIFIED_TTL_SECONDS = 1800; // 30분

    private static final String EMAIL_KEY_PREFIX = "auth:email:";

    private final JavaMailSender javaMailSender;

    private final SpringTemplateEngine templateEngine;

    private final UserRepository userRepository;

    private final RedisService redisService;

    public void sendMail(MailRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException(ErrorCode.USER_DUPLICATE_EMAIL);
        }

        String authCode = createCode();
        MimeMessage mimeMessage = createEmailMessage(request.getEmail(), authCode);

        try {
            javaMailSender.send(mimeMessage);

            String key = EMAIL_KEY_PREFIX + request.getEmail();
            redisService.setValues(key, authCode, Duration.ofMinutes(VERIFICATION_CODE_EXPIRY_MINUTES));
        } catch (MailException e) {  //JavaMailSender의 전송과정에서 오류 발생 시
            throw new BusinessException(ErrorCode.MAIL_SEND_FAILED);
        }
    }

    public String sendPasswordMail(MailRequest request) {
        String password = createNewPassword();
        MimeMessage mimeMessage = createPasswordEmailMessage(request.getEmail(), password);
        try {
            javaMailSender.send(mimeMessage);
        } catch (MailException e) {  //JavaMailSender의 전송과정에서 오류 발생 시
            throw new BusinessException(ErrorCode.MAIL_SEND_FAILED);
        }
        return password;
    }

    // 인증 번호 6자리를 구현하는 메서드
    public String createCode() {
        Random random = new Random();
        StringBuilder key = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            key.append(random.nextInt(10)); // 0~9 숫자
        }

        return key.toString();
    }

    // 임시 비밀번호를 구현하는 메서드
    public String createNewPassword() {
        Random random = new Random();
        StringBuffer key = new StringBuffer();

        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(4);

            switch (index) {
                case 0: key.append((char) ((int) random.nextInt(26) + 97)); break;
                case 1: key.append((char) ((int) random.nextInt(26) + 65)); break;
                default: key.append(random.nextInt(9));
            }
        }
        return key.toString();
    }

    private MimeMessage createEmailMessage(String recipient, String authCode) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            mimeMessageHelper.setTo(recipient);
            mimeMessageHelper.setSubject("[동방에누구] 이메일 인증을 위한 인증 코드 발송");
            mimeMessageHelper.setText(setContext(authCode), true);

            return mimeMessage;
        } catch (MessagingException e) {  // SMTP 전송 오류, 포맷 오류 발생 시
            throw new BusinessException(ErrorCode.MAIL_SEND_FAILED);
        }
    }

    private MimeMessage createPasswordEmailMessage(String recipient, String password) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            mimeMessageHelper.setTo(recipient);
            mimeMessageHelper.setSubject("[동방에누구] 임시 비밀번호 발송");
            mimeMessageHelper.setText(setPasswordContext(password), true);

            return mimeMessage;
        } catch (MessagingException e) {  // SMTP 전송 오류, 포맷 오류 발생 시
            throw new BusinessException(ErrorCode.MAIL_SEND_FAILED);
        }
    }

    public void checkAuthCode(CodeCheckRequest request) {
        String storedCode = getStoredCode(request.getEmail());
        if (storedCode == null) {
            throw new BusinessException(ErrorCode.EXPIRED_EMAIL_CODE);
        }

        // 인증 번호가 이미 인증된 상태인 경우 그냥 리턴
        if ("VERIFIED".equals(getStoredCode(request.getEmail()))){return;};

        // 입력 코드와 Redis 코드가 다르면 에러
        if (!String.valueOf(request.getAuthCode()).equals(storedCode)) {
            throw new BusinessException(ErrorCode.INVALID_EMAIL_CODE);
        }
        // 인증 성공: 값 변경 + TTL 재설정
        redisService.setValues(EMAIL_KEY_PREFIX + request.getEmail(), "VERIFIED", Duration.ofSeconds(VERIFIED_TTL_SECONDS));
    }

    public String getStoredCode(String email) {
        String key = EMAIL_KEY_PREFIX + email;
        return redisService.getValues(key);
    }

    // thymeleaf를 통한 html 적용
    public String setContext(String authCode) {
        Context context = new Context();
        context.setVariable("code", authCode);
        return templateEngine.process("AuthCode-email.html", context);
    }

    // thymeleaf를 통한 html 적용
    public String setPasswordContext(String password) {
        Context context = new Context();
        context.setVariable("password", password);
        return templateEngine.process("Password-email.html", context);
    }
}


