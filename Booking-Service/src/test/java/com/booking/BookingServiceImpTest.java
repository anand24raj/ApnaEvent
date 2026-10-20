package com.booking;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.booking.customexception.InsufficientSeatException;
import com.booking.dto.BookingRequestDto;
import com.booking.feignConfig.CustomerFeign;
import com.booking.feignConfig.EventFeign;
import com.booking.repository.BookingRepository;
import com.booking.service.BookingServiceImp;
import com.booking.service.PaymentClientService;

@ExtendWith(MockitoExtension.class)
class BookingServiceImpTest {
	@Mock
	EventFeign eventFeign;
	@Mock
	CustomerFeign customerFeign;
	@Mock
	BookingRepository bookingRepository;
	@Mock
	PaymentClientService paymentClient;
	@InjectMocks
	BookingServiceImp service;

	@Test
	void rejectsZeroSeatsBeforeCallingOtherServices() {
		var request = BookingRequestDto.builder().customerId(1L).eventId(2L).numberOfSeats(0).build();

		assertThrows(InsufficientSeatException.class, () -> service.addBooking(request));
		verifyNoInteractions(customerFeign, eventFeign, paymentClient, bookingRepository);
	}
}
