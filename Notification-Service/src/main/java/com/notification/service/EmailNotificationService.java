package com.notification.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {
    private final JavaMailSender sender;
    private final String username;
    private final String from;
    public EmailNotificationService(JavaMailSender sender,
                                    @Value("${spring.mail.username:}") String username,
                                    @Value("${app.mail.from:ApnaEvent <noreply@example.com>}") String from) {
        this.sender = sender; this.username = username; this.from = from;
    }
    public boolean send(String email, String subject, String text) {
        if (email == null || email.isBlank() || username == null || username.isBlank()) return false;
        try {
            SimpleMailMessage m = new SimpleMailMessage(); m.setFrom(from); m.setTo(email); m.setSubject(subject); m.setText(text); sender.send(m); return true;
        } catch (Exception ignored) { return false; }
    }
}

