package com.booking.service;

import java.util.List;

import com.booking.dto.BookingRequestDto;
import com.booking.dto.BookingResponseDto;


public interface BookingService {

	

	public BookingResponseDto addBooking(BookingRequestDto  booking);
	
	public BookingResponseDto viewBooking(Long id);
	
	
	public List<BookingResponseDto> viewAllBookings();
	
	
	public String deleteBookings(Long id);
	
	
	public BookingResponseDto updateBookings(BookingRequestDto booking, long id);
	
	
	public BookingResponseDto updateStatus(Long id);
	
	
	public BookingResponseDto cancleBooking(Long bookingId,Integer noOfSeat,Long paymntId);
	
	
	
}
