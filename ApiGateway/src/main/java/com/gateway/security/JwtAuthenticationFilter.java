package com.gateway.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.*;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	
	private final SecretKey key;

	public JwtAuthenticationFilter(@Value("${app.jwt.secret}") String secret) {
		if (secret.length() < 32)
			throw new IllegalArgumentException("JWT secret must contain at least 32 characters");
		key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String p = request.getRequestURI();
		return p.startsWith("/contact/") || p.equals("/contact.html") || ("GET".equalsIgnoreCase(request.getMethod()) && p.equals("/ai/suggestions")) || p.equals("/") || p.equals("/index.html") || p.startsWith("/assets/") || p.startsWith("/customer-ui/")
				|| p.startsWith("/venue-ui/") || p.startsWith("/event-ui/") || p.startsWith("/booking-ui/")
				|| p.startsWith("/payment-ui/") || p.startsWith("/notification-ui/") || p.startsWith("/auth-ui/")
				|| p.equals("/favicon.ico") || p.startsWith("/auth/")
				|| ("GET".equalsIgnoreCase(request.getMethod()) && (p.startsWith("/event/") || p.startsWith("/venue/")))
				|| p.startsWith("/actuator/") || p.startsWith("/swagger-ui/") || p.startsWith("/v3/api-docs")
				|| "OPTIONS".equalsIgnoreCase(request.getMethod());
	}

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws ServletException, IOException {
		String h = req.getHeader("Authorization");
		if (h == null || !h.startsWith("Bearer ")) {
			res.sendError(401, "Bearer token is required");
			return;
		}
		try {
			var claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(h.substring(7)).getPayload();
			String role = String.valueOf(claims.get("role"));
			String customerId = String.valueOf(claims.get("customerId"));
			var auth = new UsernamePasswordAuthenticationToken(claims.getSubject(), null,
					java.util.List.of(new SimpleGrantedAuthority("ROLE_" + role)));
			SecurityContextHolder.getContext().setAuthentication(auth);
			req.setAttribute("apnaevent.user", claims.getSubject());
			req.setAttribute("apnaevent.role", role);
			// Forward trusted gateway identity to services for ownership checks.
			chain.doFilter(new jakarta.servlet.http.HttpServletRequestWrapper(req) {
				@Override
				public String getHeader(String name) {
					if ("X-User-Name".equalsIgnoreCase(name))
						return claims.getSubject();
					if ("X-User-Role".equalsIgnoreCase(name))
						return role;
					if ("X-Customer-Id".equalsIgnoreCase(name))
						return customerId;
					return super.getHeader(name);
				}

				@Override
				public java.util.Enumeration<String> getHeaders(String name) {
					if ("X-User-Name".equalsIgnoreCase(name))
						return java.util.Collections.enumeration(java.util.List.of(claims.getSubject()));
					if ("X-User-Role".equalsIgnoreCase(name))
						return java.util.Collections.enumeration(java.util.List.of(role));
					if ("X-Customer-Id".equalsIgnoreCase(name))
						return java.util.Collections.enumeration(java.util.List.of(customerId));
					return super.getHeaders(name);
				}
			}, res);
		} catch (Exception ex) {
			res.sendError(401, "Invalid or expired token");
		}
	}
}
