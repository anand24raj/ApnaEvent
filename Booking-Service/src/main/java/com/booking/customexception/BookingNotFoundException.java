package com.booking.customexception;

public class BookingNotFoundException extends RuntimeException {
	
	
	private static final long serialVersionUID = 1L;

	public BookingNotFoundException(String msg) {
		
		super(msg);
		
	}

}
