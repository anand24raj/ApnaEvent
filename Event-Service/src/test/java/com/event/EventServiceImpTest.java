package com.event;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.event.dto.EventRequestDto;
import com.event.dto.VenueResponseDto;
import com.event.entity.Event;
import com.event.feignconfig.VenueFeign;
import com.event.repository.EventRepository;
import com.event.service.EventServiceImp;
import com.event.venue.enums.Status;

@ExtendWith(MockitoExtension.class)
class EventServiceImpTest {
	@Mock
	VenueFeign venueFeign;
	@Mock
	EventRepository eventRepository;
	@InjectMocks
	EventServiceImp service;

	@Test
	void rejectsEventWhenSeatsExceedVenueCapacity() {
		var venue = VenueResponseDto.builder().venueId(10L).capacity(50).city("Kolkata").status(Status.ACTIVE).build();
		when(venueFeign.viewVenue(10L)).thenReturn(ResponseEntity
				.ok(new com.event.response.SuccessResponse<>(venue, 200, "ok", java.time.LocalDateTime.now())));

		var request = EventRequestDto.builder().eventName("Conference").eventDate(LocalDate.now().plusDays(1))
				.eventTime(LocalTime.NOON).venueId(10L).totalSeats(51).ticketPrice(500.0).build();

		assertThrows(RuntimeException.class, () -> service.addEvent(request));
		verify(eventRepository, never()).save(any(Event.class));
	}

	@Test
	void reservesSeatsAtomicallyThroughRepository() {
		when(eventRepository.reserveSeats(eq(1L), eq(2), any())).thenReturn(1);
		assertDoesNotThrow(() -> service.updateSeats(2, 1L));
		verify(eventRepository).reserveSeats(eq(1L), eq(2), any());
	}
}
