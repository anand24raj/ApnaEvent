package com.booking.customexception;

public class PaymentProcessingException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PaymentProcessingException(String message) {
		super(message);
	}

	public PaymentProcessingException(String message, Throwable cause) {
		super(message, cause);
	}
}