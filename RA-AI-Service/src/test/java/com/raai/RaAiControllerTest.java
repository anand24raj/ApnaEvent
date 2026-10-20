package com.raai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class RaAiControllerTest {
    private static final String SECRET = "test-secret-for-apnaevent-only-32-chars";
    private RaAiController controller;
    private SecretKey key;

    @BeforeEach
    void setUp() {
        key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        controller = new RaAiController(
            "http://localhost:1241", "http://localhost:1236", "http://localhost:1235",
            "http://localhost:1237", "http://localhost:1239", "http://localhost:1240", SECRET);
    }

    @Test
    void rejectsMissingAuthentication() {
        ResponseEntity<Map<String, Object>> response = controller.ask(
            new RaAiController.AskRequest("Show my bookings"), new MockHttpServletRequest());

        assertEquals(401, response.getStatusCode().value());
        assertFalse(Boolean.TRUE.equals(response.getBody().get("externalSources")));
    }

    @Test
    void unsupportedAndPublicResearchQuestionsUseInternalDataFallback() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token("CUSTOMER", 7L));

        ResponseEntity<Map<String, Object>> response = controller.ask(
            new RaAiController.AskRequest("Find today's public news in London"), request);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(String.valueOf(response.getBody().get("message")).contains("couldn't find enough relevant information"));
        assertEquals(Boolean.FALSE, response.getBody().get("externalSources"));
    }

    @Test
    void blocksCustomerRequestsForOtherCustomerRecords() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token("CUSTOMER", 7L));

        ResponseEntity<Map<String, Object>> response = controller.ask(
            new RaAiController.AskRequest("Show all customers"), request);

        assertEquals(403, response.getStatusCode().value());
        assertTrue(String.valueOf(response.getBody().get("message")).contains("not authorized"));
    }


    @Test
    void parsesNaturalLanguageEventCategoryAndBudgetWithoutExternalSources() {
        assertEquals("technology", RaAiController.requestedCategory("Find technology events this weekend"));
        assertEquals("music", RaAiController.requestedCategory("Show music events under my budget"));
        assertEquals(1500.0, RaAiController.requestedBudget("Show music events under ₹1500"));
        assertEquals(1200.0, RaAiController.requestedBudget("Find events below 1200 rupees"));
    }

    @Test
    void parsesExplicitEventDateRangeFromQuestion() {
        var range = RaAiController.requestedDateRange("Find events on 2026-11-02");
        assertEquals(java.time.LocalDate.parse("2026-11-02"), range[0]);
        assertEquals(range[0], range[1]);
    }

    private String token(String role, Long customerId) {
        return Jwts.builder().subject("test-user").claim("role", role)
            .claim("customerId", customerId).signWith(key).compact();
    }
}
