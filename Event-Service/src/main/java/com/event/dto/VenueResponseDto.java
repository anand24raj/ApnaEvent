package com.event.dto;

import com.event.venue.enums.*;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VenueResponseDto {

	private Long venueId;

	private String venueName;

	private String city;

	private Integer capacity;

	private VenueType venueType;

	private Facilities facilities;

	private Status status;
}
