package com.venue.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse extends ApiResponse  {
	
	
	public ErrorResponse( int statusCode, String message, LocalDateTime responseTime, boolean success) {
		super(statusCode, message, responseTime,success);


	
	}


	
	
	
	
	

}
