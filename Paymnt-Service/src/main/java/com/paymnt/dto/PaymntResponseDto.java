package com.paymnt.dto;

import java.time.LocalDateTime;

import com.paymnt.enums.PaymntMode;
import com.paymnt.enums.PaymntStatus;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymntResponseDto {

	
	private Long paymentId;
	private Long bookingId;
    private Long customerId;
	
	
	private Double amount;
	
	@Enumerated(EnumType.STRING)
	private PaymntMode paymentMode;
	
      private String transactionReference;


	private PaymntStatus paymentStatus;
	 
	private  LocalDateTime    paymentDate;
}
