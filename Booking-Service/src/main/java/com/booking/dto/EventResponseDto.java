package com.booking.dto;
import java.time.*;
import com.booking.enums.Status;
import lombok.*;
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class EventResponseDto {
    private Long eventId; private String eventName; private String eventCategory;
    private LocalDate eventDate; private LocalTime eventTime; private Long venueId;
    private Integer totalSeats; private Integer availableSeats; private Double ticketPrice; private Status status;
}
