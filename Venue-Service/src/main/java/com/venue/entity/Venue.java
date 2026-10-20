package com.venue.entity;

import java.time.LocalDateTime;


import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.venue.enums.Facilities;
import com.venue.enums.Status;
import com.venue.enums.VenueType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Venue {
	
	
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	private Long venueId;
	
	private String venueName;

	@Column(length=120)
	private String managerUsername;
	
	private String city;
	
	private Integer capacity;
	
	
	@Enumerated(EnumType.STRING)
	private VenueType venueType;
	
	@Enumerated(EnumType.STRING)
	private Facilities facilities;
	
	@Enumerated(EnumType.STRING)
	private Status status;
	
	@Version
	private Integer version;
	
	@CreationTimestamp
	@Column(insertable = true, updatable = false)
	private LocalDateTime createdAt;
	
	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
	
}
