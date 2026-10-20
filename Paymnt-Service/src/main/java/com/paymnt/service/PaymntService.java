package com.paymnt.service;

import com.paymnt.dto.PaymntRequestDto;
import com.paymnt.dto.PaymntResponseDto;

public interface PaymntService {
	
	
	
	public PaymntResponseDto makePymnt(PaymntRequestDto request );
	
	
	public String refund(Long paymntId);
	
	public String getPaymntMode(Long paymntId);
	

}
