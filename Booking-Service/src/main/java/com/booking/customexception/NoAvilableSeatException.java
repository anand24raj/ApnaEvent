package com.booking.customexception;

public class NoAvilableSeatException  extends RuntimeException{
	
	
	private static final long serialVersionUID = 1L;

	public NoAvilableSeatException(String msg) {
	super(msg);
	}

}
