package com.event.controller;

import java.time.*;
import java.util.List;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import com.event.dto.*;
import com.event.response.*;
import com.event.service.EventService;

@RestController
@RequestMapping("/event")
public class EventController {
	private final EventService eService;

	public EventController(EventService eService) {
		this.eService = eService;
	}

	@PostMapping("/add")
	public ResponseEntity<?> addEvent(@RequestBody EventRequestDto request, HttpServletRequest http) {
		if ("VENUE_MANAGER".equals(http.getHeader("X-User-Role"))) request.setManagerUsername(http.getHeader("X-User-Name"));
		return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponse<>(eService.addEvent(request), 201,
				"Event created successfully", LocalDateTime.now()));
	}

	@GetMapping("/view-all")
	public ResponseEntity<?> viewAll() {
		return ResponseEntity.ok(new SuccessResponse<List<EventResponseDto>>(eService.viewAllEvents(), 200,
				"Events fetched successfully", LocalDateTime.now()));
	}

	@GetMapping("/view/{id}")
	public ResponseEntity<?> view(@PathVariable Long id) {
		return ResponseEntity.ok(
				new SuccessResponse<>(eService.viewEvent(id), 200, "Event fetched successfully", LocalDateTime.now()));
	}

	@GetMapping("/search")
	public ResponseEntity<?> search(@RequestParam(required = false) String city,
			@RequestParam(required = false) String category, @RequestParam(required = false) LocalDate date,
			@RequestParam(required = false) Double minPrice, @RequestParam(required = false) Double maxPrice,
			@RequestParam(defaultValue = "false") boolean availableOnly, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "eventDate") String sortBy,
			@RequestParam(defaultValue = "asc") String direction) {
		return ResponseEntity.ok(eService.search(city, category, date, minPrice, maxPrice, availableOnly, page, size,
				sortBy, direction));
	}

	@PutMapping("/update/{id}")
	public ResponseEntity<?> update(@RequestBody EventRequestDto request, @PathVariable Long id, HttpServletRequest http) {

		if (!canManage(id, http)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("message", "You do not have permission to manage this event."));
		return ResponseEntity.ok(new SuccessResponse<>(eService.updateEvent(request, id), 200,
				"Event updated successfully", LocalDateTime.now()));
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest http) {

		if (!canManage(id, http)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("message", "You do not have permission to manage this event."));
		return ResponseEntity.ok(new SuccessResponse<>(eService.deleteEvent(id), 200, "Event cancelled successfully",
				LocalDateTime.now()));
	}

	@PatchMapping("/update-seat/{no}/{id}")
	public ResponseEntity<String> reserve(@PathVariable Integer no, @PathVariable Long id) {

		return ResponseEntity.ok(eService.updateSeats(no, id));
	}

	@PatchMapping("/add-seat/{no}/{id}")
	public ResponseEntity<String> release(@PathVariable Integer no, @PathVariable Long id) {

		return ResponseEntity.ok(eService.addSeats(no, id));
	}
	private boolean canManage(Long id, HttpServletRequest http) {
		String role=http.getHeader("X-User-Role");
		if (!"VENUE_MANAGER".equals(role)) return true;
		return http.getHeader("X-User-Name") != null && http.getHeader("X-User-Name").equalsIgnoreCase(eService.viewEvent(id).getManagerUsername());
	}
}
