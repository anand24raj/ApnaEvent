package com.paymnt.dto;

import com.paymnt.enums.PaymntMode;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymntRequestDto {
	private Long bookingId;
	private Long customerId;
	private Double amount;
	private PaymntMode paymentMode;
}
