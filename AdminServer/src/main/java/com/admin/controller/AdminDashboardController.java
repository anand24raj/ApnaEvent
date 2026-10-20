package com.admin.controller;

import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {
	private final RestClient customer;
	private final RestClient event;
	private final RestClient booking;
	private final RestClient payment;

	public AdminDashboardController(@Value("${services.customer-url:http://localhost:1241}") String c,
			@Value("${services.event-url:http://localhost:1235}") String e,
			@Value("${services.booking-url:http://localhost:1237}") String b,
			@Value("${services.payment-url:http://localhost:1239}") String p) {
		customer = RestClient.builder().baseUrl(c).build();
		event = RestClient.builder().baseUrl(e).build();
		booking = RestClient.builder().baseUrl(b).build();
		payment = RestClient.builder().baseUrl(p).build();
	}

	@GetMapping
	public Map<String, Object> dashboard() {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("totalCustomers", count(customer, "/customer/view-all"));
		m.put("totalEvents", count(event, "/event/view-all"));
		m.put("totalBookings", count(booking, "/booking/view-all"));
		m.put("revenue", get(payment, "/paymnt/stats/revenue", Object.class));
		m.put("cancelledBookings", get(booking, "/booking/stats/cancelled", Object.class));
		m.put("failedPayments", get(payment, "/paymnt/stats/failed", Object.class));
		m.put("mostPopularEvents", get(booking, "/booking/stats/popular-events", Object.class));
		return m;
	}

	private long count(RestClient c, String path) {
		try {
			Object body = c.get().uri(path).retrieve().body(Object.class);
			if (body instanceof Map<?, ?> map && map.get("data") instanceof Collection<?> list)
				return list.size();
			if (body instanceof Collection<?> list)
				return list.size();
		} catch (Exception ignored) {
		}
		return 0;
	}

	private <T> T get(RestClient c, String path, Class<T> type) {
		try {
			return c.get().uri(path).retrieve().body(type);
		} catch (Exception e) {
			return null;
		}
	}
}
