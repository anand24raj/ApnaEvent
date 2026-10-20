package com.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter filter) throws Exception {
		return http
				.csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> auth
						.requestMatchers("/", "/index.html", "/assets/**", "/favicon.ico", "/error", "/auth/**",
								"/auth-ui/**", "/customer-ui/**", "/venue-ui/**", "/event-ui/**", "/booking-ui/**",
								"/payment-ui/**", "/notification-ui/**", "/actuator/**", "/swagger-ui/**",
								"/swagger-ui.html", "/v3/api-docs/**", "/ai/suggestions", "/contact.html", "/contact/**")
						.permitAll().requestMatchers(HttpMethod.POST, "/customer/add").hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/customer/view-all").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/customer/**").hasAnyRole("ADMIN", "CUSTOMER")
						.requestMatchers(HttpMethod.DELETE, "/customer/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/customer/view/**").authenticated()
						.requestMatchers(HttpMethod.GET, "/event/**", "/venue/**").permitAll()
						.requestMatchers("/admin/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.POST, "/event/**", "/venue/**").hasAnyRole("ADMIN", "VENUE_MANAGER")
						.requestMatchers(HttpMethod.PUT, "/event/**", "/venue/**").hasAnyRole("ADMIN", "VENUE_MANAGER")
						.requestMatchers(HttpMethod.DELETE, "/event/**", "/venue/**")
						.hasAnyRole("ADMIN", "VENUE_MANAGER").requestMatchers(HttpMethod.POST, "/booking/**")
						.hasAnyRole("CUSTOMER", "ADMIN").requestMatchers(HttpMethod.PUT, "/booking/**")
						.hasAnyRole("CUSTOMER", "ADMIN").requestMatchers(HttpMethod.PATCH, "/booking/status/**")
						.hasRole("ADMIN").requestMatchers(HttpMethod.PATCH, "/booking/**")
						.hasAnyRole("CUSTOMER", "ADMIN").requestMatchers(HttpMethod.POST, "/paymnt/pay")
						.hasRole("ADMIN").requestMatchers(HttpMethod.GET, "/paymnt/stats/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.POST, "/paymnt/**").hasAnyRole("CUSTOMER", "ADMIN")
						.requestMatchers(HttpMethod.PATCH, "/paymnt/refund/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PATCH, "/paymnt/**").hasAnyRole("CUSTOMER", "ADMIN")
						.requestMatchers(HttpMethod.POST, "/notification/**").hasRole("ADMIN")
						.requestMatchers("/notification/**").authenticated().anyRequest().authenticated())
				.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class).build();
	}
}
