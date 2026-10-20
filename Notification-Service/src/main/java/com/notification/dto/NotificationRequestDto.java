package com.notification.dto;
import com.notification.enums.NotificationType;
import lombok.*;
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class NotificationRequestDto { private Long customerId; private Long bookingId; private String message; private NotificationType notificationType; }
