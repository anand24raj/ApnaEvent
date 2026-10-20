package com.notification.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;

class ContactControllerTest {
    @Test
    void rejectsInvalidContactFormOnServer() {
        JavaMailSender sender = mock(JavaMailSender.class);
        ContactController controller = new ContactController(sender, "support@example.test", "ApnaEvent <noreply@example.test>");

        ResponseEntity<?> response = controller.submit(
            new ContactController.ContactRequest("", "not-an-email", "", "Other", "Hi", "Short"));

        assertEquals(400, response.getStatusCode().value());
        verifyNoInteractions(sender);
    }


    @Test
    void rejectsUnsupportedCategoryOnServer() {
        JavaMailSender sender = mock(JavaMailSender.class);
        ContactController controller = new ContactController(sender, "support@example.test", "ApnaEvent <noreply@example.test>");

        ResponseEntity<?> response = controller.submit(
            new ContactController.ContactRequest("Test User", "user@example.test", "", "Admin Override", "A question",
                "This is a sufficiently long test message."));

        assertEquals(400, response.getStatusCode().value());
        verifyNoInteractions(sender);
    }

    @Test
    void reportsMissingMailConfigurationInsteadOfClaimingDelivery() {
        JavaMailSender sender = mock(JavaMailSender.class);
        ContactController controller = new ContactController(sender, "", "ApnaEvent <noreply@example.test>");

        ResponseEntity<?> response = controller.submit(
            new ContactController.ContactRequest("Test User", "user@example.test", "", "Other", "A question",
                "This is a sufficiently long test message."));

        assertEquals(503, response.getStatusCode().value());
        verifyNoInteractions(sender);
    }
}
