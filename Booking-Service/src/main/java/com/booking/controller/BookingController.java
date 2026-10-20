package com.booking.controller;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.booking.dto.BookingRequestDto;
import com.booking.dto.BookingResponseDto;
import com.booking.response.ApiResponse;
import com.booking.response.SuccessResponse;
import com.booking.service.BookingService;
import com.booking.repository.BookingRepository;
import com.booking.enums.BookingStatus;

@RestController

@RequestMapping("/booking")
public class BookingController {

	private final BookingService bService;
	private final BookingRepository bRepo;

	BookingController(BookingService bService, BookingRepository bRepo) {
		this.bService = bService;
		this.bRepo = bRepo;
	}

	@PostMapping("/add")
	public ResponseEntity<?> book(@RequestBody BookingRequestDto req, jakarta.servlet.http.HttpServletRequest http) {
		if ("CUSTOMER".equals(http.getHeader("X-User-Role")) && !sameCustomer(req.getCustomerId(), http)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("message", "You can only create bookings for your own account."));
		return ResponseEntity.status(HttpStatus.CREATED).body(bService.addBooking(req));

	}


	@GetMapping("/view-all")
	public ResponseEntity<?> viewAll(jakarta.servlet.http.HttpServletRequest http) {
        var all = bService.viewAllBookings();
        if ("CUSTOMER".equals(http.getHeader("X-User-Role"))) { Long cid=parse(http.getHeader("X-Customer-Id")); all=all.stream().filter(b -> cid != null && cid.equals(b.getCustomerId())).toList(); }
        
		ApiResponse response = new SuccessResponse<>(all, HttpStatus.OK.value(),
				"Bookings fetched successfully", LocalDateTime.now());
		return ResponseEntity.ok(response);
	}

	@PutMapping("/update/{id}")
	public ResponseEntity<?> update(@RequestBody BookingRequestDto request, @PathVariable Long id, jakarta.servlet.http.HttpServletRequest http) {
        if ("CUSTOMER".equals(http.getHeader("X-User-Role")) && !ownsBooking(id,http)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("message", "You can only manage your own booking."));
		if ("CUSTOMER".equals(http.getHeader("X-User-Role")) && !sameCustomer(request.getCustomerId(), http)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("message", "You can only update your own booking."));
		ApiResponse response = new SuccessResponse<>(bService.updateBookings(request, id), HttpStatus.OK.value(),
				"Booking updated successfully", LocalDateTime.now());
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> delete(@PathVariable Long id, jakarta.servlet.http.HttpServletRequest http) {
        if ("CUSTOMER".equals(http.getHeader("X-User-Role")) && !ownsBooking(id,http)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("message", "You can only manage your own booking."));
		return ResponseEntity.ok(bService.deleteBookings(id));
	}

	@GetMapping("/view/{id}")
	public ResponseEntity<?> book(@PathVariable Long id, jakarta.servlet.http.HttpServletRequest http) {
        if ("CUSTOMER".equals(http.getHeader("X-User-Role")) && !ownsBooking(id,http)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("message", "You can only view your own booking."));

		ApiResponse success = new SuccessResponse<BookingResponseDto>(bService.viewBooking(id), HttpStatus.OK.value(),
				"booking details featched successfully", LocalDateTime.now());

		return ResponseEntity.status(HttpStatus.OK).body(success);

	}

	@PostMapping({"/cancle/{id}/{noOfseat}/{paymntId}","/cancel/{id}/{noOfseat}/{paymntId}"})
	public ResponseEntity<?> cancleBooking(@PathVariable Long id, @PathVariable Integer noOfseat, jakarta.servlet.http.HttpServletRequest http,
			@PathVariable Long paymntId) {

		if ("CUSTOMER".equals(http.getHeader("X-User-Role")) && !ownsBooking(id,http)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("message", "You can only cancel your own booking."));
		return ResponseEntity.status(HttpStatus.OK).body(bService.cancleBooking(id, noOfseat, paymntId));

	}

	@PatchMapping("status/{id}")
	public ResponseEntity<?> updateBookingStatus(@PathVariable Long id) {

		ApiResponse success = new SuccessResponse<BookingResponseDto>(bService.updateStatus(id), HttpStatus.OK.value(),
				"booking details updated successfully", LocalDateTime.now());

		return ResponseEntity.status(HttpStatus.OK).body(success);

	}


    @GetMapping("/stats/cancelled")
    public long cancelled(){ return bRepo.countByBookingStatus(BookingStatus.CANCELLED); }

    @GetMapping("/stats/popular-events")
    public java.util.List<java.util.Map<String,Object>> popular(){
        return bRepo.popularEvents().stream()
            .map(row -> java.util.Map.of("eventId", row[0], "seats", row[1])).toList();
    }
    private Long parse(String v){try{return v==null||v.isBlank()?null:Long.valueOf(v);}catch(Exception e){return null;}}
    private boolean sameCustomer(Long id,jakarta.servlet.http.HttpServletRequest h){Long cid=parse(h.getHeader("X-Customer-Id"));return id!=null&&cid!=null&&id.equals(cid);}
    private boolean ownsBooking(Long id,jakarta.servlet.http.HttpServletRequest h){Long cid=parse(h.getHeader("X-Customer-Id"));return bRepo.findById(id).map(b->cid!=null&&cid.equals(b.getCustomerId())).orElse(false);}
}
