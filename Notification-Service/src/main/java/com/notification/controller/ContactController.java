package com.notification.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/contact")
public class ContactController {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE = Pattern.compile("^[0-9+() .-]{7,25}$");
    private static final Set<String> CATEGORIES = Set.of("events", "venues", "booking", "payment",
        "account support", "organizer support", "other");

    private final JavaMailSender mailSender;
    private final String recipient;
    private final String from;

    public ContactController(
            JavaMailSender mailSender,
            @Value("${MAIL_USERNAME:}") String recipient,
            @Value("${app.mail.from:}") String from) {
        this.mailSender = mailSender;
        this.recipient = recipient == null ? "" : recipient.trim();
        this.from = from == null ? "" : from.trim();
    }

    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submit(@RequestBody(required = false) ContactRequest request) {
        String validation = validate(request);
        if (validation != null) return response(HttpStatus.BAD_REQUEST, false, validation);
        if (recipient.isBlank() || from.isBlank()) {
            return response(HttpStatus.SERVICE_UNAVAILABLE, false,
                "The contact form is not configured yet. Please use the published contact details.");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(recipient);
            message.setReplyTo(request.email().trim());
            message.setSubject("[ApnaEvent Contact] " + cleanHeader(request.subject()));
            message.setText("Name: " + request.fullName().trim()
                + "\nEmail: " + request.email().trim()
                + "\nPhone: " + clean(request.phone())
                + "\nCategory: " + request.category().trim()
                + "\n\nMessage:\n" + request.message().trim());
            mailSender.send(message);
            return response(HttpStatus.ACCEPTED, true,
                "Your enquiry was accepted by the configured email service. This does not guarantee final delivery.");
        } catch (Exception ignored) {
            return response(HttpStatus.SERVICE_UNAVAILABLE, false,
                "The email service could not accept your enquiry. Please try again later.");
        }
    }

    private String validate(ContactRequest r) {
        if (r == null) return "Please complete the contact form.";
        if (r.fullName() == null || r.fullName().trim().length() < 2 || r.fullName().trim().length() > 100)
            return "Enter a name between 2 and 100 characters.";
        if (r.email() == null || r.email().trim().length() > 254 || !EMAIL.matcher(r.email().trim()).matches())
            return "Enter a valid email address.";
        if (r.phone() != null && !r.phone().isBlank() && !PHONE.matcher(r.phone().trim()).matches())
            return "Enter a valid phone number or leave it blank.";
        if (r.category() == null || !CATEGORIES.contains(r.category().trim().toLowerCase(java.util.Locale.ROOT)))
            return "Choose a valid enquiry category.";
        if (r.subject() == null || r.subject().trim().length() < 3 || r.subject().trim().length() > 120)
            return "Enter a subject between 3 and 120 characters.";
        if (r.message() == null || r.message().trim().length() < 10 || r.message().trim().length() > 3000)
            return "Enter a message between 10 and 3000 characters.";
        return null;
    }

    private String cleanHeader(String value) {
        return value == null ? "" : value.trim().replaceAll("[\\r\\n]+", " ");
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) return "Not provided";
        return value.trim().replaceAll("[\\r\\n]+", " ");
    }

    private ResponseEntity<Map<String, Object>> response(HttpStatus status, boolean success, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", success);
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }

    public record ContactRequest(String fullName, String email, String phone, String category,
                                 String subject, String message) {}
}
