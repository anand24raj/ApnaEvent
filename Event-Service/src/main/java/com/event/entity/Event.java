package com.event.entity;

import java.time.*;
import org.hibernate.annotations.*;
import com.event.enums.Status;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "events")
public class Event {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long eventId;

	@Column(nullable = false)
	private String eventName;

	@Column(length=120)
	private String managerUsername;

	@Column(nullable = false)
	private String eventCategory;

	@Column(nullable = false)
	private LocalDate eventDate;

	@Column(nullable = false)
	private LocalTime eventTime;

	@Column(nullable = false)
	private Long venueId;

	private String venueCity;

	@Column(nullable = false)
	private Integer totalSeats;

	@Column(nullable = false)
	private Integer availableSeats;

	@Column(nullable = false)
	private Double ticketPrice;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status = Status.UPCOMING;

	@Version
	private Integer version;

	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
}
