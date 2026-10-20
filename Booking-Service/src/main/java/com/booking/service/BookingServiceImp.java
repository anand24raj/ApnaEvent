package com.booking.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.booking.customexception.BookingNotFoundException;
import com.booking.customexception.EventClosedException;
import com.booking.customexception.EventNotFoundException;
import com.booking.customexception.InsufficientSeatException;
import com.booking.customexception.PaymentProcessingException;
import com.booking.dto.BookingRequestDto;
import com.booking.dto.BookingResponseDto;
import com.booking.dto.EventResponseDto;
import com.booking.dto.PaymntRequestDto;
import com.booking.dto.PaymntResponseDto;
import com.booking.entity.Booking;
import com.booking.enums.BookingStatus;
import com.booking.enums.Status;
import com.booking.feignConfig.CustomerFeign;
import com.booking.feignConfig.EventFeign;
import com.booking.repository.BookingRepository;
import com.booking.response.SuccessResponse;

@Service
public class BookingServiceImp implements BookingService {

	private final EventFeign eventFeign;
	private final CustomerFeign customerFeign;
	private final BookingRepository bRepo;
	private final PaymentClientService paymentClient;

	public BookingServiceImp(EventFeign eventFeign, CustomerFeign customerFeign, BookingRepository bRepo,
			PaymentClientService paymentClient) {

		this.eventFeign = eventFeign;
		this.customerFeign = customerFeign;
		this.bRepo = bRepo;
		this.paymentClient = paymentClient;
	}

	@Override
	@Transactional(noRollbackFor = PaymentProcessingException.class)
	public BookingResponseDto addBooking(BookingRequestDto request) {

		validate(request);

		customerFeign.viewCustomer(request.getCustomerId());

		EventResponseDto event = event(request.getEventId());

		if (event.getStatus() == Status.COMPLETED || event.getStatus() == Status.CANCELLED) {

			throw new EventClosedException("Booking is not allowed for a completed or cancelled event");
		}

		if (event.getAvailableSeats() == null || event.getAvailableSeats() < request.getNumberOfSeats()) {

			throw new InsufficientSeatException("Not enough seats available");
		}

		if (!Boolean.TRUE.equals(reserveSeats(request.getNumberOfSeats(), request.getEventId()))) {

			throw new InsufficientSeatException("Seats are no longer available");
		}

		Booking booking = new Booking();

		BeanUtils.copyProperties(request, booking);

		booking.setBookingDate(request.getBookingDate() == null ? LocalDate.now() : request.getBookingDate());

		booking.setTotalAmount(request.getNumberOfSeats() * event.getTicketPrice());

		booking.setBookingStatus(BookingStatus.PENDING);

		booking = bRepo.save(booking);

		PaymntResponseDto paidPayment = null;

		try {

			if (request.getPaymentMode() == null) {

				releaseSeats(booking);

				booking.setBookingStatus(BookingStatus.PAYMENT_FAILED);
				bRepo.save(booking);

				throw new PaymentProcessingException("Payment method is required");
			}

			SuccessResponse<PaymntResponseDto> payment;

			try {

				payment = paymentClient.pay(new PaymntRequestDto(booking.getBookingId(), booking.getCustomerId(),
						booking.getTotalAmount(), request.getPaymentMode())).join();

			} catch (RuntimeException ex) {

				throw new PaymentProcessingException("Payment could not be completed right now. Please try again.", ex);
			}

			paidPayment = payment.getData();

			if (payment.getData() == null
					|| !"COMPLETED".equalsIgnoreCase(String.valueOf(payment.getData().getPaymentStatus()))) {

				releaseSeats(booking);

				booking.setBookingStatus(BookingStatus.PAYMENT_FAILED);
				bRepo.save(booking);

				throw new PaymentProcessingException("Payment could not be completed right now. Please try again.");
			}

			booking.setBookingStatus(BookingStatus.APPROVED);

			booking = bRepo.save(booking);

			return toResponse(booking);

		} catch (RuntimeException ex) {

			/*
			 * If payment was completed but a later operation failed, attempt to refund the
			 * payment.
			 */
			if (paidPayment != null && paidPayment.getPaymentId() != null) {

				try {
					paymentClient.refund(paidPayment.getPaymentId());
				} catch (Exception ignored) {
					// Do not hide the original booking exception.
				}
			}

			/*
			 * If booking is still pending, release the reserved seats and persist
			 * PAYMENT_FAILED.
			 */
			if (booking.getBookingStatus() == BookingStatus.PENDING) {

				releaseSeats(booking);

				booking.setBookingStatus(BookingStatus.PAYMENT_FAILED);

				bRepo.save(booking);
			}

			throw ex;
		}
	}

	private Boolean reserveSeats(int seats, long eventId) {

		try {

			var response = eventFeign.updateSeats(seats, eventId);

			if (response == null || !response.getStatusCode().is2xxSuccessful()) {

				throw new InsufficientSeatException("Event Service could not reserve the selected seats");
			}

			return true;

		} catch (feign.FeignException ex) {

			String detail = ex.contentUTF8();

			throw new InsufficientSeatException(
					detail == null || detail.isBlank() ? "Event Service rejected the seat reservation"
							: "Event Service: " + detail);

		} catch (InsufficientSeatException ex) {

			throw ex;

		} catch (Exception ex) {

			throw new InsufficientSeatException(
					"Event Service is temporarily unavailable. " + "Refresh the event list and try again.");
		}
	}

	private void releaseSeats(Booking booking) {

		try {

			eventFeign.addSeats(booking.getNumberOfSeats(), booking.getEventId());

		} catch (Exception ignored) {
			// Seat release failure should not hide the original exception.
		}
	}

	@Override
	public BookingResponseDto viewBooking(Long id) {

		return toResponse(
				bRepo.findById(id).orElseThrow(() -> new BookingNotFoundException("Booking not found: " + id)));
	}

	@Override
	public List<BookingResponseDto> viewAllBookings() {

		return bRepo.findAll().stream().map(this::toResponse).toList();
	}

	@Override
	@Transactional
	public String deleteBookings(Long id) {

		Booking booking = bRepo.findById(id)
				.orElseThrow(() -> new BookingNotFoundException("Booking not found: " + id));

		if (booking.getBookingStatus() != BookingStatus.CANCELLED) {
			releaseSeats(booking);
		}

		bRepo.delete(booking);

		return "Booking deleted: " + id;
	}

	@Override
	@Transactional
	public BookingResponseDto updateBookings(BookingRequestDto request, long id) {

		Booking booking = bRepo.findById(id)
				.orElseThrow(() -> new BookingNotFoundException("Booking not found: " + id));

		if (booking.getBookingStatus() != BookingStatus.PENDING) {

			throw new IllegalStateException("Only pending bookings can be updated");
		}

		EventResponseDto event = event(booking.getEventId());

		int difference = request.getNumberOfSeats() - booking.getNumberOfSeats();

		if (difference > 0) {

			if (!reserveSeats(difference, booking.getEventId())) {

				throw new InsufficientSeatException("Not enough seats");
			}

		} else if (difference < 0) {

			eventFeign.addSeats(-difference, booking.getEventId());
		}

		booking.setNumberOfSeats(request.getNumberOfSeats());

		booking.setTotalAmount(booking.getNumberOfSeats() * event.getTicketPrice());

		return toResponse(bRepo.save(booking));
	}

	@Override
	@Transactional
	public BookingResponseDto updateStatus(Long id) {

		Booking booking = bRepo.findById(id)
				.orElseThrow(() -> new BookingNotFoundException("Booking not found: " + id));

		booking.setBookingStatus(BookingStatus.APPROVED);

		return toResponse(bRepo.save(booking));
	}

	@Override
	@Transactional
	public BookingResponseDto cancleBooking(Long bookingId, Integer noOfSeat, Long paymentId) {

		Booking booking = bRepo.findById(bookingId)
				.orElseThrow(() -> new BookingNotFoundException("Booking not found"));

		EventResponseDto event = event(booking.getEventId());

		LocalDateTimeHolder.ensureBeforeEvent(event);

		if (noOfSeat == null || noOfSeat <= 0 || noOfSeat > booking.getNumberOfSeats()) {

			throw new InsufficientSeatException("Invalid cancellation seat count");
		}

		eventFeign.addSeats(noOfSeat, booking.getEventId());

		if (paymentId != null) {
			paymentClient.refund(paymentId);
		}

		booking.setNumberOfSeats(booking.getNumberOfSeats() - noOfSeat);

		if (booking.getNumberOfSeats() == 0) {

			booking.setBookingStatus(BookingStatus.CANCELLED);
		}

		booking = bRepo.save(booking);

		return toResponse(booking);
	}

	private EventResponseDto event(long id) {

		var response = eventFeign.viewEvent(id);

		if (response == null || response.getBody() == null || response.getBody().getData() == null) {

			throw new EventNotFoundException("Event not found: " + id);
		}

		return response.getBody().getData();
	}

	private BookingResponseDto toResponse(Booking booking) {

		BookingResponseDto response = BookingResponseDto.builder().build();

		BeanUtils.copyProperties(booking, response);

		return response;
	}

	private void validate(BookingRequestDto request) {

		if (request.getCustomerId() == null || request.getEventId() == null) {

			throw new IllegalArgumentException("Customer and event are required");
		}

		if (request.getNumberOfSeats() == null || request.getNumberOfSeats() <= 0) {

			throw new InsufficientSeatException("Seats must be greater than zero");
		}
	}

	private static class LocalDateTimeHolder {

		static void ensureBeforeEvent(EventResponseDto event) {

			if (event.getEventDate() != null && event.getEventTime() != null && java.time.LocalDateTime.now()
					.isAfter(java.time.LocalDateTime.of(event.getEventDate(), event.getEventTime()))) {

				throw new EventClosedException("Cancellation is not allowed after the event");
			}
		}
	}
}