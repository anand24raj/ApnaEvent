package com.booking.dto;
import java.time.LocalDate;
import com.booking.enums.PaymntMode;
import lombok.*;
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class BookingRequestDto {
    private Long customerId; private Long eventId; private Integer numberOfSeats;
    private LocalDate bookingDate; private PaymntMode paymentMode;
}
