package com.event.feignconfig;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.event.dto.VenueResponseDto;
import com.event.response.SuccessResponse;

@FeignClient(name = "Venue-Service", configuration = com.event.config.FeignCorrelationConfig.class)
public interface VenueFeign {
	@GetMapping("/venue/view/{id}")
	ResponseEntity<SuccessResponse<VenueResponseDto>> viewVenue(@PathVariable Long id);
}
