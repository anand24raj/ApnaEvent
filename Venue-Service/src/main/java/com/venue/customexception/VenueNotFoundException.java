package com.venue.customexception;

public class VenueNotFoundException  extends RuntimeException{
	
	
	private static final long serialVersionUID = 1L;

	public VenueNotFoundException(String msg) {
		
		super(msg);
		
	}

}
