package com.event.customexception;

public class VenueValidationException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public VenueValidationException(String message) {
		super(message);
	}
}
