package com.event.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignCorrelationConfig {
	@Bean
	RequestInterceptor correlationInterceptor() {
		return template -> {
			try {
				var attrs = RequestContextHolder.getRequestAttributes();
				if (attrs instanceof ServletRequestAttributes servlet) {
					String id = servlet.getRequest().getHeader("X-Correlation-Id");
					if (id != null && !id.isBlank()) {
						template.header("X-Correlation-Id", id);
					}
				}
			} catch (Exception ignored) {
				// Correlation propagation must never break the business request.
			}
		};
	}
}
