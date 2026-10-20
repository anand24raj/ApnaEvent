package com.venue.dto;
import com.venue.enums.*;
import lombok.*;
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class VenueRequestDto {
    private String venueName; private String city; private String managerUsername; private Integer capacity;
    private VenueType venueType; private Facilities facilities; private Status status;
}
