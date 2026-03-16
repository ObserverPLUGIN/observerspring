package com.yangtheory.observerspring.common.security.user;

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
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(
        name = "observer_accounts",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_observer_accounts_login_id", columnNames = "login_id"),
                @UniqueConstraint(name = "uk_observer_accounts_email", columnNames = "email")
        },
        indexes = {
                @Index(name = "idx_observer_accounts_email_verified", columnList = "email_verified"),
                @Index(name = "idx_observer_accounts_last_login_at", columnList = "last_login_at")
        }
)
public class ObserverAccount extends BaseTimeEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "observer_account_seq_gen")
    @SequenceGenerator(name = "observer_account_seq_gen", sequenceName = "observer_account_seq", allocationSize = 1)
    private Long id;

    @Column(name = "login_id", nullable = false, length = 120)
    private String loginId;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "display_name", nullable = false, length = 80)
    private String displayName;

    @Column(name = "password_hash", nullable = false, length = 120)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private ObserverRole role;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    protected ObserverAccount() {
    }

    public ObserverAccount(
            String loginId,
            String email,
            String displayName,
            String passwordHash,
            ObserverRole role
    ) {
        this.loginId = loginId;
        this.email = email;
        this.displayName = displayName;
        this.passwordHash = passwordHash;
        this.role = role;
        this.emailVerified = false;
        this.enabled = false;
    }

    public Long getId() {
        return id;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ObserverRole getRole() {
        return role;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public OffsetDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void activateEmailVerified() {
        this.emailVerified = true;
        this.enabled = true;
    }

    public void activateDirectly() {
        this.emailVerified = true;
        this.enabled = true;
    }

    public void changePassword(String nextPasswordHash) {
        this.passwordHash = nextPasswordHash;
    }

    public void recordLogin(OffsetDateTime at) {
        this.lastLoginAt = at;
    }

    public boolean matchesIdentifier(String identifier) {
        return loginId.equalsIgnoreCase(identifier) || email.equalsIgnoreCase(identifier);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return loginId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
