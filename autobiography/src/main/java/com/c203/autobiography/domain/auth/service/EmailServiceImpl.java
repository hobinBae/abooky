package com.c203.autobiography.domain.auth.service;

import com.c203.autobiography.domain.auth.dto.ForgotPasswordRequest;
import com.c203.autobiography.domain.member.entity.Member;
import com.c203.autobiography.domain.member.repository.MemberRepository;
import com.c203.autobiography.global.exception.ApiException;
import com.c203.autobiography.global.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final String resetBaseUrl = "http://localhost:8080/reset-password?token=";
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final static String RESET_TOKEN_PREFIX = "reset:";

    @Override
    public void sendPasswordResetEmail(ForgotPasswordRequest request) {

        Member member = memberRepository.findByEmailAndName(
                        request.getEmail(),
                        request.getName()
                )
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(RESET_TOKEN_PREFIX + token, member.getMemberId().toString(), Duration.ofMinutes(15));
        log.info("레디스 저장 확인: key={}, value={}", RESET_TOKEN_PREFIX + token, member.getMemberId());
        String subject = "비밀번호 재설정 링크";
        String resetUrl = resetBaseUrl + token;

        String htmlContent = "<p>비밀번호를 재설정하려면 아래 링크를 클릭하세요:</p>" +
                "<p><a href=\"" + resetUrl + "\">비밀번호 재설정 링크</a></p>" +
                "<p>링크는 <strong>15분 후 만료</strong>됩니다.</p>";
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(request.getEmail());
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("비밀번호 재설정 토큰 token={}, MemberId={}", token, member.getMemberId());
        } catch (MessagingException e) {
            log.error("메일 전송 실패", e);
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
