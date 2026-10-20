package com.notification.dto;
import java.time.LocalDateTime;
import com.notification.enums.*;
import lombok.*;
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class NotificationResponseDto {
 private Long notificationId; private Long customerId; private Long bookingId; private String message;
 private NotificationType notificationType; private NotificationStatus notificationStatus; private LocalDateTime createdAt;
}
