package com.event.config;

import java.time.LocalDateTime;
import java.util.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.slf4j.MDC;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handle(Exception ex, HttpServletRequest req) {
		int status = HttpStatus.BAD_REQUEST.value();
		if (ex instanceof com.event.customexception.EventNotFoundException)
			status = HttpStatus.NOT_FOUND.value();
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", LocalDateTime.now());
		body.put("status", status);
		body.put("error", HttpStatus.valueOf(status).getReasonPhrase());
		body.put("message", ex.getMessage());
		body.put("path", req.getRequestURI());
		body.put("correlationId", MDC.get("correlationId"));
		return ResponseEntity.status(status).body(body);
	}
}
