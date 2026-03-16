package com.yangtheory.observerspring.common.security.email;

import com.yangtheory.observerspring.common.persistence.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "observer_email_codes",
        indexes = {
                @Index(name = "idx_observer_email_codes_email_purpose", columnList = "email,purpose"),
                @Index(name = "idx_observer_email_codes_expires_at", columnList = "expires_at")
        }
)
public class EmailCodeTicket extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "observer_email_code_seq_gen")
    @SequenceGenerator(
            name = "observer_email_code_seq_gen",
            sequenceName = "observer_email_code_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 30)
    private EmailCodePurpose purpose;

    @Column(name = "auth_code", nullable = false, length = 6)
    private String authCode;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "consumed_at")
    private OffsetDateTime consumedAt;

    protected EmailCodeTicket() {
    }

    public EmailCodeTicket(String email, EmailCodePurpose purpose, String authCode, OffsetDateTime expiresAt) {
        this.email = email;
        this.purpose = purpose;
        this.authCode = authCode;
        this.expiresAt = expiresAt;
    }

    public String getEmail() {
        return email;
    }

    public EmailCodePurpose getPurpose() {
        return purpose;
    }

    public String getAuthCode() {
        return authCode;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired() {
        return expiresAt.isBefore(OffsetDateTime.now());
    }

    public boolean isConsumed() {
        return consumedAt != null;
    }

    public void consume() {
        this.consumedAt = OffsetDateTime.now();
    }
}
