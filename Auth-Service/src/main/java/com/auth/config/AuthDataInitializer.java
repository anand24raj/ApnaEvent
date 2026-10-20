package com.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.auth.entity.UserAccount;
import com.auth.repository.UserAccountRepository;

@Configuration
public class AuthDataInitializer {

    @Bean
    CommandLineRunner seedAdmin(
            UserAccountRepository repo,
            PasswordEncoder encoder,
            @Value("${app.auth.username:admin}") String username,
            @Value("${app.auth.password:}") String password) {
        return args -> {
            String adminUsername = username == null ? "" : username.trim();
            if (adminUsername.isEmpty()) {
                throw new IllegalStateException(
                        "AUTH_USERNAME must be configured before the initial administrator can be created.");
            }

            var existingAccount = repo.findByUsernameIgnoreCase(adminUsername);
            if (existingAccount.isPresent()) {
                UserAccount account = existingAccount.get();
                // A previous version could have seeded the administrator with an empty password.
                // Rotate that known-weak credential rather than leaving it usable.
                if (account.getPasswordHash() != null && encoder.matches("", account.getPasswordHash())) {
                    if (password == null || password.isBlank()) {
                        throw new IllegalStateException(
                                "The configured administrator account has an empty password. Set AUTH_PASSWORD "
                                        + "to a strong, unique value so Auth-Service can rotate it, then restart.");
                    }
                    account.setPasswordHash(encoder.encode(password));
                    repo.save(account);
                }
                return;
            }

            if (password == null || password.isBlank()) {
                throw new IllegalStateException(
                        "AUTH_PASSWORD must be configured before the initial administrator can be created. "
                                + "Set a strong, unique password and restart Auth-Service.");
            }

            repo.save(UserAccount.builder()
                    .username(adminUsername)
                    .passwordHash(encoder.encode(password))
                    .role("ADMIN")
                    .enabled(true)
                    .build());
        };
    }
}
