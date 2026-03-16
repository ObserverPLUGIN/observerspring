package com.yangtheory.observerspring.common.security.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ObserverAccountRepository extends JpaRepository<ObserverAccount, Long> {

    @Query("""
            select account
            from ObserverAccount account
            where lower(account.loginId) = lower(:identifier)
               or lower(account.email) = lower(:identifier)
            """)
    Optional<ObserverAccount> findByIdentifier(@Param("identifier") String identifier);

    Optional<ObserverAccount> findByLoginIdIgnoreCase(String loginId);

    Optional<ObserverAccount> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
