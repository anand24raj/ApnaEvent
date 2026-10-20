package com.paymnt.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import com.paymnt.dto.*;
import com.paymnt.entity.Paymnt;
import com.paymnt.enums.*;
import com.paymnt.exception.PaymntNotProcessException;
import com.paymnt.repository.PaymntRepository;

@Service
public class PaymntServiceImp implements PaymntService {
	private final PaymntRepository repo;

	public PaymntServiceImp(PaymntRepository repo) {
		this.repo = repo;
	}

	@Override
	@Transactional
	@CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
	@Retry(name = "paymentService")
	public PaymntResponseDto makePymnt(PaymntRequestDto request) {
		if (request.getBookingId() == null || request.getPaymentMode() == null)
			throw new PaymntNotProcessException("Booking ID and payment method are required");
		var existing = repo.findByBookingId(request.getBookingId());
		if (existing.isPresent()) {
			Paymnt prior = existing.get();
			if (prior.getPaymentStatus() == PaymntStatus.COMPLETED)
				return toResponse(prior);
			if (prior.getPaymentStatus() == PaymntStatus.REFUNDED)
				throw new PaymntNotProcessException("This booking has already been refunded");
		}
		Paymnt p = new Paymnt();
		BeanUtils.copyProperties(request, p);
		p.setPaymentDate(LocalDateTime.now());
		// Booking service owns the calculated amount; payment receives it through this
		// request in production.
		// For this implementation, amount is required from request.
		if (request.getAmount() == null || request.getAmount() <= 0)
			throw new PaymntNotProcessException("Payment amount must be greater than zero");
		p.setAmount(request.getAmount());
		p.setTransactionReference(reference());
		p.setPaymentStatus(PaymntStatus.COMPLETED);
		Paymnt saved = repo.save(p);
		return toResponse(saved);
	}

	public PaymntResponseDto paymentFallback(PaymntRequestDto request, Throwable ex) {

		org.slf4j.LoggerFactory.getLogger(PaymntServiceImp.class).error(
				"PAYMENT FALLBACK TRIGGERED. bookingId={}, customerId={}, amount={}, paymentMode={}",
				request != null ? request.getBookingId() : null, request != null ? request.getCustomerId() : null,
				request != null ? request.getAmount() : null, request != null ? request.getPaymentMode() : null, ex);

		throw new PaymntNotProcessException("Payment Service is temporarily unavailable");
	}

	@Override
	@Transactional
	public String refund(Long id) {
		Paymnt p = repo.findById(id).orElseThrow(() -> new PaymntNotProcessException("Payment not found: " + id));
		p.setPaymentStatus(PaymntStatus.REFUNDED);
		repo.save(p);
		return "Refund initiated successfully";
	}

	@Override
	public String getPaymntMode(Long id) {
		return repo.findById(id).orElseThrow(() -> new PaymntNotProcessException("Payment not found: " + id))
				.getPaymentStatus().name();
	}

	private String reference() {
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		SecureRandom r = new SecureRandom();
		StringBuilder s = new StringBuilder("TXN");
		for (int i = 0; i < 10; i++)
			s.append(chars.charAt(r.nextInt(chars.length())));
		return s.toString();
	}

	private PaymntResponseDto toResponse(Paymnt p) {
		PaymntResponseDto r = PaymntResponseDto.builder().build();
		BeanUtils.copyProperties(p, r);
		return r;
	}
}
