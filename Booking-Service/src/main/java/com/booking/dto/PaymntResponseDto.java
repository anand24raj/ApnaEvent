package com.booking.dto;
import java.time.LocalDateTime;
import com.booking.enums.PaymntMode;
import lombok.*;
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class PaymntResponseDto { private Long paymentId; private Long bookingId; private Double amount; private PaymntMode paymentMode; private String transactionReference; private String paymentStatus; private LocalDateTime paymentDate; }
