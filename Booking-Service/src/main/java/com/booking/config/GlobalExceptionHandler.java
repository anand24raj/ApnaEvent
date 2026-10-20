package com.booking.config;

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
		if (ex instanceof com.booking.customexception.BookingNotFoundException)
			status = HttpStatus.NOT_FOUND.value();
		else if (ex instanceof com.booking.customexception.InsufficientSeatException
				|| ex instanceof com.booking.customexception.NoAvilableSeatException)
			status = HttpStatus.CONFLICT.value();
		else if (ex instanceof com.booking.customexception.EventClosedException)
			status = HttpStatus.UNPROCESSABLE_CONTENT.value();
		else if (ex instanceof com.booking.customexception.PaymentProcessingException)
			status = HttpStatus.UNPROCESSABLE_CONTENT.value();
		else if (ex instanceof IllegalArgumentException)
			status = HttpStatus.BAD_REQUEST.value();
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", LocalDateTime.now());
		body.put("status", status);
		body.put("error", HttpStatus.valueOf(status).getReasonPhrase());
		log.error("Booking request failed: {} {}", req.getMethod(), req.getRequestURI(), ex);
		String message = (ex instanceof com.booking.customexception.PaymentProcessingException
				|| ex instanceof com.booking.customexception.InsufficientSeatException
				|| ex instanceof com.booking.customexception.NoAvilableSeatException
				|| ex instanceof com.booking.customexception.EventClosedException
				|| ex instanceof com.booking.customexception.BookingNotFoundException
				|| ex instanceof IllegalArgumentException) ? ex.getMessage()
						: "The booking could not be completed right now. Please try again.";
		body.put("message", message);
		body.put("path", req.getRequestURI());
		body.put("correlationId", MDC.get("correlationId"));
		return ResponseEntity.status(status).body(body);
	}
}
