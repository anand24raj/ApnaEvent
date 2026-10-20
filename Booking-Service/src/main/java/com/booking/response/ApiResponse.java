package com.booking.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;


@AllArgsConstructor
@Data
public class ApiResponse {

	private int statusCode;
	
	private String message;
	
	private LocalDateTime responseTime;
	
	
	
	
	
}
