package com.booking.dto;

import java.time.LocalDate;

import com.booking.enums.BookingStatus;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BookingResponseDto {

	
	private Long bookingId;
	private Long customerId;
	private Long eventId;
	private Integer numberOfSeats;
	private LocalDate
	 bookingDate;
	private Double totalAmount;
	
	
	@Enumerated(EnumType.STRING)
	private BookingStatus bookingStatus;
}
