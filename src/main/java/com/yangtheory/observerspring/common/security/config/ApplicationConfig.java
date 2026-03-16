package com.yangtheory.observerspring.common.security.config;

import com.yangtheory.observerspring.common.security.user.ObserverAccount;
import com.yangtheory.observerspring.common.security.user.ObserverAccountRepository;
import com.yangtheory.observerspring.common.security.user.ObserverRole;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ApplicationConfig {
    @Value("${observer.security.admin.username}")
    private String adminUsername;

    @Value("${observer.security.admin.email:admin@observer.local}")
    private String adminEmail;

    @Value("${observer.security.admin.display-name:Observer Admin}")
    private String adminDisplayName;

    @Value("${observer.security.admin.password}")
    private String adminPassword;

    private final ObserverAccountRepository observerAccountRepository;

    public ApplicationConfig(ObserverAccountRepository observerAccountRepository) {
        this.observerAccountRepository = observerAccountRepository;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return identifier -> observerAccountRepository.findByIdentifier(identifier.trim())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CommandLineRunner bootstrapAdminInitializer(PasswordEncoder passwordEncoder) {
        return args -> {
            String normalizedLoginId = normalizeIdentifier(adminUsername);
            String normalizedEmail = adminEmail.trim().toLowerCase(Locale.ROOT);

            boolean alreadyExists = observerAccountRepository.findByLoginIdIgnoreCase(normalizedLoginId).isPresent()
                    || observerAccountRepository.findByEmailIgnoreCase(normalizedEmail).isPresent();
            if (alreadyExists) {
                return;
            }

            ObserverAccount adminAccount = new ObserverAccount(
                    normalizedLoginId,
                    normalizedEmail,
                    adminDisplayName.trim(),
                    passwordEncoder.encode(adminPassword),
                    ObserverRole.ADMIN
            );
            adminAccount.activateDirectly();
            observerAccountRepository.save(adminAccount);
        };
    }

    private String normalizeIdentifier(String identifier) {
        return identifier == null ? "" : identifier.trim().toLowerCase(Locale.ROOT);
    }
}
