package com.event.dto;

import java.time.*;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventRequestDto {
	private String eventName;
	private String managerUsername;
	private String eventCategory;
	private LocalDate eventDate;
	private LocalTime eventTime;
	private Long venueId;
	private Integer totalSeats;
	private Double ticketPrice;
}
