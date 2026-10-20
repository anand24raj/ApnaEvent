package com.event.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import com.event.dto.*;

public interface EventService {
	EventResponseDto addEvent(EventRequestDto event);

	EventResponseDto viewEvent(Long id);

	List<EventResponseDto> viewAllEvents();

	String deleteEvent(Long id);

	EventResponseDto updateEvent(EventRequestDto event, long id);

	String updateSeats(Integer seats, long id);

	String addSeats(Integer seats, long id);

	Page<EventResponseDto> search(String city, String category, LocalDate date, Double minPrice, Double maxPrice,
			boolean availableOnly, int page, int size, String sortBy, String direction);

	void updateLifecycle();
}
