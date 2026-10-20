package com.venue.dto;

import com.venue.enums.Facilities;
import com.venue.enums.Status;
import com.venue.enums.VenueType;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
@AllArgsConstructor
public class VenueResponseDto {

	
private Long venueId;
	
	private String venueName;
	private String managerUsername;
	private String city;
	private Integer capacity;
	
	
	@Enumerated(EnumType.STRING)
	private VenueType venueType;
	@Enumerated(EnumType.STRING)
	private Facilities facilities;
	@Enumerated(EnumType.STRING)
	private Status status;
}
