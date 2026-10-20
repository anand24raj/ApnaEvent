package com.paymnt.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.paymnt.enums.PaymntMode;
import com.paymnt.enums.PaymntStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Paymnt {

	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	private Long paymentId;

	@Column(nullable = false, unique = true)
	private Long bookingId;

	@Column(nullable = false)
	private Long customerId;

	private Double amount;

	@Enumerated(EnumType.STRING)
	private PaymntMode paymentMode;

	private String transactionReference;

	@Enumerated(EnumType.STRING)
	private PaymntStatus paymentStatus;

	private LocalDateTime paymentDate;

	@Version
	private Integer version;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

}
