package com.paymnt.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@Data
@NoArgsConstructor
public class ApiResponse {

	private int statusCode;
	
	private String message;
	
	private LocalDateTime responseTime;
	

	
	
	
}
