package com.event.service;

import java.time.*;
import java.util.*;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.event.customexception.*;
import com.event.dto.*;
import com.event.entity.Event;
import com.event.enums.Status;
import com.event.feignconfig.VenueFeign;
import com.event.repository.EventRepository;

@Service
public class EventServiceImp implements EventService {
	private final VenueFeign venueFeign;
	private final EventRepository eRepo;

	public EventServiceImp(VenueFeign venueFeign, EventRepository eRepo) {
		this.venueFeign = venueFeign;
		this.eRepo = eRepo;
	}

	@Override
	@Transactional
	public EventResponseDto addEvent(EventRequestDto request) {
		validate(request);
		VenueResponseDto venue = getVenue(request.getVenueId());
		if (venue.getCapacity() == null || request.getTotalSeats() > venue.getCapacity()) {
			throw new VenueValidationException("Event seats cannot exceed venue capacity");
		}
		if (venue.getStatus() != null && !"ACTIVE".equalsIgnoreCase(venue.getStatus().name())) {
			throw new VenueValidationException("Venue is not active");
		}
		Event event = new Event();
		BeanUtils.copyProperties(request, event);
		event.setAvailableSeats(request.getTotalSeats());
		event.setVenueCity(venue.getCity());
		event.setStatus(Status.UPCOMING);
		return toResponse(eRepo.save(event));
	}

	@Override
	public EventResponseDto viewEvent(Long id) {
		return toResponse(eRepo.findById(id).orElseThrow(() -> new EventNotFoundException("Event not found: " + id)));
	}

	@Override
	public List<EventResponseDto> viewAllEvents() {
		return eRepo.findAll().stream().map(this::toResponse).toList();
	}

	public Page<EventResponseDto> search(String city, String category, LocalDate date, Double minPrice, Double maxPrice,
			boolean availableOnly, int page, int size, String sortBy, String direction) {
		Sort sort = Sort.by(direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
		return eRepo.search(city, category, date, minPrice, maxPrice, availableOnly, PageRequest.of(page, size, sort))
				.map(this::toResponse);
	}

	@Override
	public String deleteEvent(Long id) {
		Event event = eRepo.findById(id).orElseThrow(() -> new EventNotFoundException("Event not found: " + id));
		event.setStatus(Status.CANCELLED);
		eRepo.save(event);
		return "Event cancelled: " + id;
	}

	@Override
	@Transactional
	public EventResponseDto updateEvent(EventRequestDto request, long id) {
		Event event = eRepo.findById(id).orElseThrow(() -> new EventNotFoundException("Event not found: " + id));
		validate(request);
		VenueResponseDto venue = getVenue(request.getVenueId());
		if (request.getTotalSeats() > venue.getCapacity())
			throw new VenueValidationException("Seats exceed venue capacity");
		int bookedSeats = event.getTotalSeats() - event.getAvailableSeats();
		String manager = event.getManagerUsername();
		BeanUtils.copyProperties(request, event);
		if (request.getManagerUsername() == null || request.getManagerUsername().isBlank()) event.setManagerUsername(manager);
		event.setVenueCity(venue.getCity());
		event.setAvailableSeats(Math.max(0, request.getTotalSeats() - bookedSeats));
		return toResponse(eRepo.save(event));
	}

	@Transactional
	public String updateSeats(Integer seats, long id) {
		validateSeats(seats);

		int updated = eRepo.reserveSeats(id, seats, List.of(Status.COMPLETED, Status.CANCELLED));

		if (updated == 0) {
			throw new EventClosedException("Event is closed or does not have enough seats");
		}

		return "Reserved " + seats + " seat(s)";
	}

	@Override
	@Transactional
	public String addSeats(Integer seats, long id) {
		validateSeats(seats);
		if (eRepo.releaseSeats(id, seats) == 0)
			throw new EventNotFoundException("Event not found or capacity exceeded");
		return "Released " + seats + " seat(s)";
	}

	@Transactional
	public void updateLifecycle() {
		LocalDateTime now = LocalDateTime.now();
		for (Event e : eRepo.findAll()) {
			LocalDateTime start = LocalDateTime.of(e.getEventDate(), e.getEventTime());
			Status next = e.getStatus();
			if (e.getStatus() != Status.CANCELLED) {
				if (now.isBefore(start))
					next = Status.UPCOMING;
				else if (now.isBefore(start.plusHours(3)))
					next = Status.ONGOING;
				else
					next = Status.COMPLETED;
			}
			if (next != e.getStatus()) {
				e.setStatus(next);
				eRepo.save(e);
			}
		}
	}

	private VenueResponseDto getVenue(Long id) {
		var body = venueFeign.viewVenue(id).getBody();
		if (body != null && body.getData() != null)
			return body.getData();
		throw new VenueValidationException("Venue not found: " + id);
	}

	private void validate(EventRequestDto r) {
		if (r.getEventName() == null || r.getEventName().isBlank())
			throw new IllegalArgumentException("Event name is required");
		if (r.getEventDate() == null || r.getEventTime() == null)
			throw new IllegalArgumentException("Event date and time are required");
		if (r.getVenueId() == null)
			throw new IllegalArgumentException("Venue is required");
		if (r.getTotalSeats() == null || r.getTotalSeats() <= 0)
			throw new IllegalArgumentException("Total seats must be greater than zero");
		if (r.getTicketPrice() == null || r.getTicketPrice() < 0)
			throw new IllegalArgumentException("Ticket price cannot be negative");
	}

	private void validateSeats(Integer seats) {
		if (seats == null || seats <= 0)
			throw new IllegalArgumentException("Seats must be greater than zero");
	}

	private EventResponseDto toResponse(Event e) {
		EventResponseDto r = new EventResponseDto();
		BeanUtils.copyProperties(e, r);
		return r;
	}
}
