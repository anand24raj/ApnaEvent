package com.booking.feignConfig;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.booking.dto.EventResponseDto;
import com.booking.response.SuccessResponse;

@FeignClient("Event-Service")
public interface EventFeign {

	@GetMapping("/event/view/{id}")
	ResponseEntity<SuccessResponse<EventResponseDto>> viewEvent(@PathVariable Long id);

	/*
	 * Event-Service returns plain text from these endpoints. Therefore the Feign
	 * contract must explicitly expect String.
	 */
	@PatchMapping("/event/update-seat/{no}/{id}")
	ResponseEntity<String> updateSeats(@PathVariable Integer no, @PathVariable Long id);

	@PatchMapping("/event/add-seat/{no}/{id}")
	ResponseEntity<String> addSeats(@PathVariable Integer no, @PathVariable Long id);
}