package com.event.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.event.enums.Status;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventResponseDto {

	private Long eventId;

	private String eventName;
	private String managerUsername;

	private String eventCategory;

	private LocalDate eventDate;

	private LocalTime eventTime;

	private Long venueId;

	private Integer totalSeats;

	private Integer availableSeats;

	private Double ticketPrice;

	@Enumerated(EnumType.STRING)
	private Status status;

}
