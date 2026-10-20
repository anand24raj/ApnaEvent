package com.booking.entity;
import java.time.LocalDate;
import com.booking.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
@Entity @Data @NoArgsConstructor
@Table(name="bookings")
public class Booking {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long bookingId;
 @Column(nullable=false) private Long customerId;
 @Column(nullable=false) private Long eventId;
 @Column(nullable=false) private Integer numberOfSeats;
 @Column(nullable=false) private LocalDate bookingDate;
 @Column(nullable=false) private Double totalAmount;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private BookingStatus bookingStatus;
 @Version private Integer version;
}
