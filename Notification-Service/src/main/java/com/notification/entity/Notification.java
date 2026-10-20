package com.notification.entity;

import java.time.LocalDateTime;

import com.notification.enums.NotificationStatus;
import com.notification.enums.NotificationType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {

	@SequenceGenerator(name = "gen6", sequenceName = "notification-seq", allocationSize = 1, initialValue = 10000000)
	
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen6")
	@Id
	
	private Long notificationId;
	private Long customerId;
	private Long bookingId;
	private String message;
	
	
	@Enumerated(EnumType.STRING)
	private NotificationType notificationType;
	@Enumerated(EnumType.STRING)

	private NotificationStatus
	notificationStatus;
	
	private LocalDateTime    createdAt;
}
