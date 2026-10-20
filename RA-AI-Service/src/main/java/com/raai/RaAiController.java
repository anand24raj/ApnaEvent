package com.raai;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/ai")
public class RaAiController {
    private static final Pattern BUDGET_PATTERN = Pattern.compile("(?:under|below|less than|at most|max(?:imum)?(?: budget)?|budget(?: of)?|₹|rs\\.?\\s*)\\s*₹?\\s*(\\d{2,9})(?:\\.\\d{1,2})?", Pattern.CASE_INSENSITIVE);
    private static final String FALLBACK =
        "I can answer questions using ApnaEvent's available project data only. " +
        "I couldn't find enough relevant information in the available internal data to answer that question.";

    private final Map<String, RestClient> services = new LinkedHashMap<>();
    private final SecretKey jwtKey;

    public RaAiController(
            @Value("${services.customer-url}") String customer,
            @Value("${services.venue-url}") String venue,
            @Value("${services.event-url}") String event,
            @Value("${services.booking-url}") String booking,
            @Value("${services.payment-url}") String payment,
            @Value("${services.notification-url}") String notification,
            @Value("${app.jwt.secret}") String jwtSecret) {
        services.put("customers", RestClient.builder().baseUrl(customer).build());
        services.put("venues", RestClient.builder().baseUrl(venue).build());
        services.put("events", RestClient.builder().baseUrl(event).build());
        services.put("bookings", RestClient.builder().baseUrl(booking).build());
        services.put("payments", RestClient.builder().baseUrl(payment).build());
        services.put("notifications", RestClient.builder().baseUrl(notification).build());
        if (jwtSecret == null || jwtSecret.length() < 32) {
            throw new IllegalArgumentException("JWT secret must contain at least 32 characters");
        }
        this.jwtKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/suggestions")
    public List<Map<String, String>> suggestions() {
        return List.of(
            Map.of("label", "🎟 Find available events", "question", "Show upcoming events with available seats"),
            Map.of("label", "💻 Tech this weekend", "question", "Find technology events this weekend"),
            Map.of("label", "💸 Events under ₹1500", "question", "Show events under ₹1500"),
            Map.of("label", "🏛 Find venues", "question", "Show available venues"),
            Map.of("label", "📋 My bookings", "question", "Show my bookings"),
            Map.of("label", "💳 Payment summary", "question", "Show my payment status"),
            Map.of("label", "🪑 Events with seats", "question", "Which events have available seats?"),
            Map.of("label", "❓ What can you do?", "question", "What can RA help me with?")
        );
    }

    @PostMapping("/ask")
    public ResponseEntity<Map<String, Object>> ask(
            @RequestBody AskRequest request,
            HttpServletRequest httpRequest) {
        UserContext user;
        try {
            user = authenticate(httpRequest.getHeader("Authorization"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(answer("Please sign in with a valid ApnaEvent account to use RA.", "error", List.of()));
        }

        String question = request == null || request.question() == null
            ? "" : request.question().trim();
        String q = question.toLowerCase(Locale.ROOT);
        if (q.isBlank()) {
            return ResponseEntity.badRequest()
                .body(answer("Please ask a question about ApnaEvent.", "text", List.of()));
        }

        try {
            Map<String, Object> result;
            if (any(q, "what can", "what do you do", "help", "capabilities")) {
                result = answer("I can look up events and venues in ApnaEvent, and show booking or payment information permitted for your account. I do not perform public-web research or invent missing information.", "text", List.of());
            } else if (any(q, "event", "availability", "seat")) {
                result = events(user, q);
            } else if (any(q, "venue", "capacity", "place")) {
                result = venues(user);
            } else if (any(q, "my profile", "my account", "my details")) {
                if (!user.isAdmin() && !user.isCustomer()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(answer("Your account is not authorized to view customer profile data.", "error", List.of()));
                }
                result = profile(user);
            } else if (any(q, "customer", "member", "all users", "all customers")) {
                if (!user.isAdmin()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(answer("You are not authorized to view other customer records.", "error", List.of()));
                }
                result = customers(user);
            } else if (any(q, "booking", "reservation")) {
                if (!user.isAdmin() && (!user.isCustomer() || user.customerId() == null)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(answer("Your account is not authorized to view booking records.", "error", List.of()));
                }
                result = bookings(user);
            } else if (any(q, "payment", "revenue", "refund")) {
                if (!user.isAdmin() && (!user.isCustomer() || user.customerId() == null)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(answer("Your account is not authorized to view payment records.", "error", List.of()));
                }
                result = payments(user);
            } else if (any(q, "book", "reserve")) {
                result = answer("To make a booking, sign in and use ApnaEvent's existing booking flow. RA does not create bookings or initiate payments.", "action", List.of(Map.of("label", "Open booking", "url", "/booking-ui/form.html")));
            } else if (any(q, "register", "sign up", "create account")) {
                result = answer("Use ApnaEvent's registration page to create an account.", "action", List.of(Map.of("label", "Create account", "url", "/auth-ui/register.html")));
            } else {
                result = answer(FALLBACK, "text", List.of());
            }
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            // Never return exception messages, downstream payloads, or infrastructure details to the browser.
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(answer("RA could not read the required ApnaEvent data right now. Please try again when the relevant service is available.", "error", List.of()));
        }
    }

    private Map<String, Object> events(UserContext user, String question) {
        List<Map<String, Object>> rows = maps(get("events", "/event/view-all", user));
        List<Map<String, Object>> available = rows.stream()
            .filter(x -> number(x.get("availableSeats")) > 0 && !closed(x.get("status")))
            .toList();

        String category = requestedCategory(question);
        Double maxBudget = requestedBudget(question);
        LocalDate[] dates = requestedDateRange(question);
        List<Map<String, Object>> items = available.stream()
            .filter(x -> category == null || containsIgnoreCase(x.get("eventCategory"), category)
                || containsIgnoreCase(x.get("eventName"), category))
            .filter(x -> maxBudget == null || priceAtMost(x.get("ticketPrice"), maxBudget))
            .filter(x -> dates == null || dateInRange(x.get("eventDate"), dates[0], dates[1]))
            .limit(8).toList();

        if (items.isEmpty()) {
            String criteria = category == null && maxBudget == null && dates == null
                ? " with available seats" : " matching your requested filters";
            return answer("No ApnaEvent events were found" + criteria + ". Try another category, date, or budget.", "events", List.of());
        }
        StringBuilder message = new StringBuilder("Here are verified events with available seats from ApnaEvent");
        if (category != null) message.append(" matching category or title '").append(category).append("'");
        if (maxBudget != null) message.append(" priced at or below ₹").append(maxBudget.intValue());
        if (dates != null) message.append(" between ").append(dates[0]).append(" and ").append(dates[1]);
        message.append(".");
        return answer(message.toString(), "events", items);
    }

    static String requestedCategory(String question) {
        String[] knownTerms = {"technology", "tech", "music", "workshop", "workshops", "education",
            "sports", "sport", "business", "art", "arts", "food", "comedy", "conference", "concert"};
        for (String term : knownTerms) if (question.contains(term)) return term;
        Matcher matcher = Pattern.compile("(?:category|type)\\s*[:=]?\\s*([a-z][a-z -]{1,30})", Pattern.CASE_INSENSITIVE).matcher(question);
        if (matcher.find()) return matcher.group(1).trim();
        return null;
    }

    static Double requestedBudget(String question) {
        Matcher matcher = BUDGET_PATTERN.matcher(question);
        if (!matcher.find()) return null;
        try { return Double.valueOf(matcher.group(1)); }
        catch (NumberFormatException ignored) { return null; }
    }

    static LocalDate[] requestedDateRange(String question) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        if (question.contains("today")) return new LocalDate[] {today, today};
        if (question.contains("tomorrow")) {
            LocalDate tomorrow = today.plusDays(1);
            return new LocalDate[] {tomorrow, tomorrow};
        }
        if (question.contains("this weekend") || question.contains("weekend")) {
            LocalDate saturday = today.with(DayOfWeek.SATURDAY);
            if (today.getDayOfWeek() == DayOfWeek.SUNDAY) saturday = today.minusDays(1);
            else if (today.isAfter(saturday)) saturday = saturday.plusWeeks(1);
            return new LocalDate[] {saturday, saturday.plusDays(1)};
        }
        Matcher matcher = Pattern.compile("\\b(20\\d{2}-\\d{2}-\\d{2})\\b").matcher(question);
        if (matcher.find()) {
            try {
                LocalDate date = LocalDate.parse(matcher.group(1));
                return new LocalDate[] {date, date};
            } catch (java.time.DateTimeException ignored) { return null; }
        }
        return null;
    }

    static boolean containsIgnoreCase(Object value, String needle) {
        return value != null && String.valueOf(value).toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
    }

    static boolean priceAtMost(Object value, double budget) {
        try { return Double.parseDouble(String.valueOf(value)) <= budget; }
        catch (NumberFormatException ignored) { return false; }
    }

    static boolean dateInRange(Object value, LocalDate start, LocalDate end) {
        if (value == null) return false;
        try {
            LocalDate date = value instanceof LocalDate d ? d : LocalDate.parse(String.valueOf(value));
            return !date.isBefore(start) && !date.isAfter(end);
        } catch (java.time.DateTimeException ignored) { return false; }
    }

    private Map<String, Object> venues(UserContext user) {
        List<Map<String, Object>> rows = maps(get("venues", "/venue/view-all", user));
        List<Map<String, Object>> items = rows.stream()
            .filter(x -> "ACTIVE".equalsIgnoreCase(String.valueOf(x.get("status"))))
            .limit(8).toList();
        return answer(items.isEmpty()
            ? "No active venues were found in the current ApnaEvent data."
            : "Here are active venues from ApnaEvent.", "venues", items);
    }

    private Map<String, Object> profile(UserContext user) {
        if (!user.isCustomer() && !user.isAdmin()) {
            return answer("Your account is not authorized to view customer profile data.", "error", List.of());
        }
        if (user.customerId() == null) {
            return answer("A customer profile is not associated with this account in the available token.", "text", List.of());
        }
        Object data = get("customers", "/customer/view/" + user.customerId(), user);
        List<Map<String, Object>> rows = maps(data);
        if (rows.isEmpty() && data instanceof Map<?, ?> m) {
            Map<String, Object> row = new LinkedHashMap<>();
            m.forEach((k, v) -> row.put(String.valueOf(k), v));
            rows.add(row);
        }
        return answer(rows.isEmpty() ? FALLBACK : "Here is the profile returned for your authenticated ApnaEvent account.", "profile", rows);
    }

    private Map<String, Object> customers(UserContext user) {
        List<Map<String, Object>> rows = maps(get("customers", "/customer/view-all", user));
        return answer(rows.isEmpty() ? "No customer records were returned by ApnaEvent." : "Customer records returned by the authorized ApnaEvent service.", "customers", rows.stream().limit(8).toList());
    }

    private Map<String, Object> bookings(UserContext user) {
        if (!user.isAdmin() && (!user.isCustomer() || user.customerId() == null)) {
            return answer("Your account is not authorized to view booking records.", "error", List.of());
        }
        List<Map<String, Object>> rows = maps(get("bookings", "/booking/view-all", user));
        return answer(rows.isEmpty() ? "No bookings were found for the authorized account in ApnaEvent." : "Here are booking records visible to your ApnaEvent account.", "bookings", rows.stream().limit(8).toList());
    }

    private Map<String, Object> payments(UserContext user) {
        if (!user.isAdmin() && (!user.isCustomer() || user.customerId() == null)) {
            return answer("Your account is not authorized to view payment records.", "error", List.of());
        }
        if (user.isAdmin()) {
            Object revenue = get("payments", "/paymnt/stats/revenue", user);
            Object failed = get("payments", "/paymnt/stats/failed", user);
            return answer("ApnaEvent payment summary: completed revenue ₹" + revenue + "; failed payments " + failed + ".", "payment", List.of());
        }
        List<Map<String, Object>> rows = maps(get("payments", "/paymnt/view-all", user));
        return answer(rows.isEmpty()
            ? "No payment records were found for your authenticated account in ApnaEvent."
            : "Here are payment records visible to your authenticated ApnaEvent account.", "payments", rows.stream().limit(8).toList());
    }

    private Object get(String service, String path, UserContext user) {
        Object response = services.get(service).get().uri(path)
            .headers(headers -> {
                headers.set("X-User-Role", user.role());
                if (user.customerId() != null) headers.set("X-Customer-Id", String.valueOf(user.customerId()));
                headers.set("X-User-Name", user.subject());
            })
            .retrieve().body(Object.class);
        if (response instanceof Map<?, ?> body && body.containsKey("data")) return body.get("data");
        return response;
    }

    private UserContext authenticate(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing bearer token");
        }
        try {
            Claims claims = Jwts.parser().verifyWith(jwtKey).build()
                .parseSignedClaims(authorization.substring(7)).getPayload();
            String role = String.valueOf(claims.get("role"));
            if (role == null || role.isBlank() || "null".equals(role)) throw new IllegalArgumentException("Missing role");
            Long customerId = null;
            Object rawId = claims.get("customerId");
            if (rawId != null) {
                try { customerId = Long.valueOf(String.valueOf(rawId)); }
                catch (NumberFormatException ignored) { /* non-customer role */ }
            }
            return new UserContext(claims.getSubject(), role, customerId);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid bearer token");
        }
    }

    private List<Map<String, Object>> maps(Object value) {
        if (!(value instanceof List<?> list)) return new ArrayList<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                Map<String, Object> row = new LinkedHashMap<>();
                map.forEach((k, v) -> row.put(String.valueOf(k), v));
                rows.add(row);
            }
        }
        return rows;
    }

    private long number(Object value) {
        try { return Long.parseLong(String.valueOf(value)); }
        catch (Exception ignored) { return 0; }
    }

    private boolean closed(Object value) {
        String status = String.valueOf(value);
        return "CANCELLED".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status);
    }

    private boolean any(String value, String... terms) {
        for (String term : terms) if (value.contains(term)) return true;
        return false;
    }

    private Map<String, Object> answer(String message, String type, List<?> items) {
        return Map.of("message", message, "type", type, "items", items,
            "source", "ApnaEvent project data", "externalSources", false);
    }

    private record UserContext(String subject, String role, Long customerId) {
        boolean isAdmin() { return "ADMIN".equalsIgnoreCase(role); }
        boolean isCustomer() { return "CUSTOMER".equalsIgnoreCase(role); }
    }

    public record AskRequest(String question) {}
}
