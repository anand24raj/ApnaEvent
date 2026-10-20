package com.booking.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.booking.entity.Booking;
import com.booking.enums.BookingStatus;
public interface BookingRepository extends JpaRepository<Booking,Long> {
 long countByBookingStatus(BookingStatus status);
    @org.springframework.data.jpa.repository.Query("select b.eventId as eventId, sum(b.numberOfSeats) as seats from Booking b where b.bookingStatus = com.booking.enums.BookingStatus.APPROVED group by b.eventId order by seats desc")
    java.util.List<Object[]> popularEvents();
}
