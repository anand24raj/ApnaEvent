package com.auth.config;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.auth.entity.UserAccount;
import com.auth.repository.UserAccountRepository;

@ExtendWith(MockitoExtension.class)
class AuthDataInitializerTest {

    @Mock
    UserAccountRepository repository;

    @Mock
    PasswordEncoder encoder;

    @Test
    void refusesToCreateInitialAdminWhenPasswordIsBlank() {
        when(repository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.empty());
        CommandLineRunner runner = new AuthDataInitializer().seedAdmin(repository, encoder, "admin", " ");

        assertThrows(IllegalStateException.class, () -> runner.run());
        verify(repository, never()).save(any(UserAccount.class));
    }

    @Test
    void rotatesAnExistingEmptyPasswordWhenConfiguredPasswordIsAvailable() throws Exception {
        UserAccount existing = UserAccount.builder()
                .username("admin").passwordHash("empty-password-hash").role("ADMIN").enabled(true).build();
        when(repository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(existing));
        when(encoder.matches("", "empty-password-hash")).thenReturn(true);
        when(encoder.encode("Strong-Local-Password-123!")).thenReturn("new-hash");
        CommandLineRunner runner = new AuthDataInitializer().seedAdmin(
                repository, encoder, "admin", "Strong-Local-Password-123!");

        runner.run();

        verify(repository).save(existing);
        org.junit.jupiter.api.Assertions.assertEquals("new-hash", existing.getPasswordHash());
    }

    @Test
    void refusesToKeepAnExistingEmptyPasswordWhenNoReplacementIsConfigured() {
        UserAccount existing = UserAccount.builder()
                .username("admin").passwordHash("empty-password-hash").role("ADMIN").enabled(true).build();
        when(repository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(existing));
        when(encoder.matches("", "empty-password-hash")).thenReturn(true);
        CommandLineRunner runner = new AuthDataInitializer().seedAdmin(repository, encoder, "admin", "");

        assertThrows(IllegalStateException.class, () -> runner.run());
        verify(repository, never()).save(any(UserAccount.class));
    }

    @Test
    void doesNotRequireSeedPasswordWhenAdminAlreadyExists() throws Exception {
        when(repository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(UserAccount.builder()
                .username("admin").passwordHash("already-hashed").role("ADMIN").enabled(true).build()));
        CommandLineRunner runner = new AuthDataInitializer().seedAdmin(repository, encoder, "admin", "");

        runner.run();
        verify(repository, never()).save(any(UserAccount.class));
    }
}
