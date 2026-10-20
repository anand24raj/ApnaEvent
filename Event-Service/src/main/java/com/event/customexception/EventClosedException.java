package com.event.customexception;

public class EventClosedException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public EventClosedException(String message) {
		super(message);
	}
}
