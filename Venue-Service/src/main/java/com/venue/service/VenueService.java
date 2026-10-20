package com.venue.service;

import java.util.List;

import com.venue.dto.VenueRequestDto;
import com.venue.dto.VenueResponseDto;



public interface VenueService {
	
	

	public VenueResponseDto addVenue(VenueRequestDto  venue);
	
	public VenueResponseDto viewVenue(Long id);
	
	
	public List<VenueResponseDto> viewAllVenues();
	
	
	public String deleteVenue(Long id);
	
	
	public VenueResponseDto updateVenue(VenueRequestDto venue, long id);
	
	

}
