package com.notification.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.Map;

@Component
public class CustomerClient {
    private final RestClient client;
    public CustomerClient(@Value("${services.customer-url:http://localhost:1241}") String url) { client = RestClient.builder().baseUrl(url).build(); }
    public String email(Long id) {
        try {
            Object body = client.get().uri("/customer/view/{id}", id).retrieve().body(Object.class);
            if (body instanceof Map<?,?> m && m.get("data") instanceof Map<?,?> d) return String.valueOf(d.get("email"));
        } catch (Exception ignored) { }
        return null;
    }
}
