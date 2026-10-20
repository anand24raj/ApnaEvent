package com.customer.dto;

import com.customer.enums.MemberShip;
import com.customer.enums.Status;

import lombok.Data;

@Data



public class CustomerResponseDto {

	
	
	private Long custommerId;
	
	
	
	private String customerName;
	
	
	
	private String email;
	
	

	private String mobile;
	
	

	private String city;
	
	
	private MemberShip memberShipType;
	
	private Status status;

	
	
}
