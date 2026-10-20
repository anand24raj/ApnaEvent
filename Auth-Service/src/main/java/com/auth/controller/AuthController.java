package com.auth.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import com.auth.entity.UserAccount;
import com.auth.repository.UserAccountRepository;
import com.auth.security.JwtService;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final JwtService jwtService;
    private final UserAccountRepository accounts;
    private final PasswordEncoder encoder;
    private final RestClient customerClient;

    public AuthController(JwtService jwtService, UserAccountRepository accounts, PasswordEncoder encoder,
                          @Value("${services.customer-url:http://localhost:1241}") String customerUrl) {
        this.jwtService = jwtService;
        this.accounts = accounts;
        this.encoder = encoder;
        this.customerClient = RestClient.builder().baseUrl(customerUrl).build();
    }

    @PostMapping("/token")
    public ResponseEntity<Map<String,String>> token(@RequestBody LoginRequest request) {
        if (request.username() == null || request.password() == null) return ResponseEntity.status(401).build();
        UserAccount account = accounts.findByUsernameIgnoreCase(request.username().trim()).orElse(null);
        if (account == null || !account.isEnabled() || !encoder.matches(request.password(), account.getPasswordHash()))
            return ResponseEntity.status(401).build();
        return ResponseEntity.ok(Map.of("token", jwtService.createToken(account.getUsername(), account.getRole(), account.getCustomerId()),
                "tokenType", "Bearer", "role", account.getRole(), "customerId", account.getCustomerId() == null ? "" : account.getCustomerId().toString()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request.username() == null || request.username().isBlank() || request.password() == null || request.password().length() < 8)
            return ResponseEntity.badRequest().body(Map.of("message", "Username and a password of at least 8 characters are required."));
        final String role = "CUSTOMER";
        String username = request.username().trim().toLowerCase();
        if (accounts.existsByUsernameIgnoreCase(username))
            return ResponseEntity.status(409).body(Map.of("message", "Username already exists."));

        try {
            var customerBody = customerClient.post().uri("/customer/add").body(Map.of(
                    "customerName", request.fullName(), "email", request.email(), "mobile", request.mobile(), "city", request.city()
            )).retrieve().body(java.util.Map.class);
            Long customerId = null;
            if (customerBody != null && customerBody.get("data") instanceof java.util.Map<?,?> data && data.get("custommerId") != null) customerId = Long.valueOf(String.valueOf(data.get("custommerId")));
            accounts.save(UserAccount.builder().username(username).passwordHash(encoder.encode(request.password()))
                    .role(role).customerId(customerId).enabled(true).build());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Account created successfully. You can sign in now.", "role", role));
        } catch (Exception ex) {
            return ResponseEntity.status(400).body(Map.of("message", "Customer profile could not be created. Please verify your details and try again."));
        }
    }

    public record LoginRequest(String username, String password) {}
    public record RegisterRequest(String username, String password, String fullName, String email, String mobile, String city) {}
}
