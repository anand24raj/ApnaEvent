package com.paymnt.config;

import java.time.LocalDateTime;
import java.util.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handle(Exception ex, HttpServletRequest req) {
		int status = HttpStatus.BAD_REQUEST.value();
		if (ex instanceof com.paymnt.exception.PaymntNotProcessException)
			status = HttpStatus.UNPROCESSABLE_CONTENT.value();
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", LocalDateTime.now());
		body.put("status", status);
		body.put("error", HttpStatus.valueOf(status).getReasonPhrase());
		log.error("Payment request failed: {} {}", req.getMethod(), req.getRequestURI(), ex);
		body.put("message", ex instanceof com.paymnt.exception.PaymntNotProcessException ? ex.getMessage()
				: "Payment could not be completed right now. Please try again.");
		body.put("path", req.getRequestURI());
		body.put("correlationId", MDC.get("correlationId"));
		return ResponseEntity.status(status).body(body);
	}
}
