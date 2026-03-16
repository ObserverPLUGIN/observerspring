package com.yangtheory.observerspring.common.security.email;

import com.yangtheory.observerspring.common.security.auth.InvalidEmailCodeException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailCodeService {
    private static final Logger log = LoggerFactory.getLogger(EmailCodeService.class);

    private final EmailCodeTicketRepository emailCodeTicketRepository;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${observer.auth.code-expiration-minutes:10}")
    private long codeExpirationMinutes;

    @Value("${observer.auth.expose-debug-code:false}")
    private boolean exposeDebugCode;

    @Value("${observer.mail.from:noreply@observer.local}")
    private String mailFrom;

    public EmailCodeService(
            EmailCodeTicketRepository emailCodeTicketRepository,
            ObjectProvider<JavaMailSender> mailSenderProvider
    ) {
        this.emailCodeTicketRepository = emailCodeTicketRepository;
        this.mailSenderProvider = mailSenderProvider;
    }

    @Transactional
    public String issueCode(String rawEmail, EmailCodePurpose purpose) {
        String email = normalizeEmail(rawEmail);
        String code = String.format("%06d", secureRandom.nextInt(1_000_000));

        emailCodeTicketRepository.deleteByEmailIgnoreCaseAndPurpose(email, purpose);
        emailCodeTicketRepository.save(new EmailCodeTicket(
                email,
                purpose,
                code,
                OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(codeExpirationMinutes)
        ));

        sendCode(email, purpose, code);
        return code;
    }

    @Transactional
    public void verifyCode(String rawEmail, EmailCodePurpose purpose, String inputCode) {
        String email = normalizeEmail(rawEmail);
        EmailCodeTicket ticket = emailCodeTicketRepository
                .findFirstByEmailIgnoreCaseAndPurposeAndConsumedAtIsNullOrderByExpiresAtDesc(email, purpose)
                .orElseThrow(() -> new InvalidEmailCodeException("인증 코드가 없거나 이미 만료되었습니다."));

        if (ticket.isExpired()) {
            throw new InvalidEmailCodeException("인증 코드가 만료되었습니다. 다시 요청해 주세요.");
        }

        if (!ticket.getAuthCode().equals(inputCode)) {
            throw new InvalidEmailCodeException("인증 코드가 올바르지 않습니다.");
        }

        ticket.consume();
    }

    public String exposeDebugCode(String issuedCode) {
        return exposeDebugCode ? issuedCode : null;
    }

    private void sendCode(String email, EmailCodePurpose purpose, String code) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();

        if (mailSender == null) {
            log.info("Mail sender is not configured. purpose={}, email={}, code={}", purpose, email, code);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(email);
        message.setSubject(subjectFor(purpose));
        message.setText(bodyFor(purpose, code));

        try {
            mailSender.send(message);
            log.info("Sent {} code to {}", purpose, email);
        } catch (Exception exception) {
            log.warn(
                    "Failed to send {} mail to {}. Falling back to logs. code={}, reason={}",
                    purpose,
                    email,
                    code,
                    exception.getMessage()
            );
        }
    }

    private String subjectFor(EmailCodePurpose purpose) {
        return switch (purpose) {
            case REGISTER -> "[Observer] 회원가입 이메일 인증";
            case RESET_PASSWORD -> "[Observer] 비밀번호 재설정 인증";
        };
    }

    private String bodyFor(EmailCodePurpose purpose, String code) {
        return switch (purpose) {
            case REGISTER -> "Observer 관리자 계정 이메일 인증 코드: " + code + "\n10분 안에 입력해 주세요.";
            case RESET_PASSWORD -> "Observer 비밀번호 재설정 코드: " + code + "\n10분 안에 입력해 주세요.";
        };
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
