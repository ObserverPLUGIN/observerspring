package com.yangtheory.observerspring.common.security.auth;

import com.yangtheory.observerspring.common.security.config.JwtService;
import com.yangtheory.observerspring.common.security.email.EmailCodePurpose;
import com.yangtheory.observerspring.common.security.email.EmailCodeService;
import com.yangtheory.observerspring.common.security.user.ObserverAccount;
import com.yangtheory.observerspring.common.security.user.ObserverAccountRepository;
import com.yangtheory.observerspring.common.security.user.ObserverRole;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ObserverAccountRepository observerAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailCodeService emailCodeService;

    public AuthenticationService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            ObserverAccountRepository observerAccountRepository,
            PasswordEncoder passwordEncoder,
            EmailCodeService emailCodeService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.observerAccountRepository = observerAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailCodeService = emailCodeService;
    }

    @Transactional
    public AuthActionResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (observerAccountRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateEmailException("이미 가입된 이메일 주소입니다.");
        }

        ObserverAccount account = new ObserverAccount(
                normalizedEmail,
                normalizedEmail,
                request.displayName().trim(),
                passwordEncoder.encode(request.password()),
                ObserverRole.OPERATOR
        );
        observerAccountRepository.save(account);

        String issuedCode = emailCodeService.issueCode(normalizedEmail, EmailCodePurpose.REGISTER);
        return new AuthActionResponse(
                "회원가입이 완료되었습니다. 이메일로 받은 6자리 인증 코드를 입력해 주세요.",
                normalizedEmail,
                "VERIFY_EMAIL",
                emailCodeService.exposeDebugCode(issuedCode)
        );
    }

    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        ObserverAccount account = observerAccountRepository.findByIdentifier(normalizeIdentifier(request.username()))
                .orElseThrow(() -> new BadCredentialsException("Bad credentials"));

        if (!account.isEmailVerified()) {
            throw new EmailVerificationRequiredException("이메일 인증을 먼저 완료해 주세요.");
        }

        if (!account.isEnabled()) {
            throw new DisabledException("비활성화된 계정입니다.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username().trim(), request.password())
        );

        String accessToken = jwtService.generateToken(account);
        account.recordLogin(OffsetDateTime.now(ZoneOffset.UTC));

        return new AuthenticationResponse(
                accessToken,
                "Bearer",
                account.getUsername(),
                account.getDisplayName(),
                account.getEmail()
        );
    }

    @Transactional
    public AuthActionResponse resendVerificationEmail(EmailRequest request) {
        ObserverAccount account = observerAccountRepository.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .orElseThrow(() -> new BadCredentialsException("등록된 계정을 찾을 수 없습니다."));

        if (account.isEmailVerified()) {
            return new AuthActionResponse(
                    "이미 이메일 인증이 완료된 계정입니다.",
                    account.getEmail(),
                    "LOGIN",
                    null
            );
        }

        String issuedCode = emailCodeService.issueCode(account.getEmail(), EmailCodePurpose.REGISTER);
        return new AuthActionResponse(
                "인증 메일을 다시 보냈습니다. 메일함을 확인해 주세요.",
                account.getEmail(),
                "VERIFY_EMAIL",
                emailCodeService.exposeDebugCode(issuedCode)
        );
    }

    @Transactional
    public AuthActionResponse verifyEmail(VerifyEmailRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        ObserverAccount account = observerAccountRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("등록된 계정을 찾을 수 없습니다."));

        emailCodeService.verifyCode(normalizedEmail, EmailCodePurpose.REGISTER, request.code());
        account.activateEmailVerified();

        return new AuthActionResponse(
                "이메일 인증이 완료되었습니다. 이제 로그인할 수 있습니다.",
                normalizedEmail,
                "LOGIN",
                null
        );
    }

    @Transactional
    public AuthActionResponse requestPasswordReset(EmailRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        ObserverAccount account = observerAccountRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);
        if (account == null) {
            return new AuthActionResponse(
                    "입력한 이메일로 비밀번호 재설정 안내를 보냈습니다.",
                    normalizedEmail,
                    "RESET_PASSWORD",
                    null
            );
        }

        String issuedCode = emailCodeService.issueCode(normalizedEmail, EmailCodePurpose.RESET_PASSWORD);
        return new AuthActionResponse(
                "비밀번호 재설정 코드를 이메일로 보냈습니다.",
                normalizedEmail,
                "RESET_PASSWORD",
                emailCodeService.exposeDebugCode(issuedCode)
        );
    }

    @Transactional
    public AuthActionResponse resetPassword(PasswordResetRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        ObserverAccount account = observerAccountRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("등록된 계정을 찾을 수 없습니다."));

        emailCodeService.verifyCode(normalizedEmail, EmailCodePurpose.RESET_PASSWORD, request.code());
        account.changePassword(passwordEncoder.encode(request.newPassword()));

        return new AuthActionResponse(
                "비밀번호가 재설정되었습니다. 새 비밀번호로 로그인해 주세요.",
                normalizedEmail,
                "LOGIN",
                null
        );
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse currentUser(Authentication authentication) {
        ObserverAccount account = (ObserverAccount) authentication.getPrincipal();
        return new CurrentUserResponse(
                account.getUsername(),
                account.getEmail(),
                account.getDisplayName(),
                account.getRole().name()
        );
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeIdentifier(String identifier) {
        return identifier == null ? "" : identifier.trim().toLowerCase(Locale.ROOT);
    }
}
