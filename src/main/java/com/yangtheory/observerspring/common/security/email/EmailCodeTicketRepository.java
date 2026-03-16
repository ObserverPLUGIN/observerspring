package com.yangtheory.observerspring.common.security.email;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailCodeTicketRepository extends JpaRepository<EmailCodeTicket, Long> {

    void deleteByEmailIgnoreCaseAndPurpose(String email, EmailCodePurpose purpose);

    Optional<EmailCodeTicket> findFirstByEmailIgnoreCaseAndPurposeAndConsumedAtIsNullOrderByExpiresAtDesc(
            String email,
            EmailCodePurpose purpose
    );
}
