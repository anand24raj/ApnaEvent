package com.paymnt.controller;

import java.util.Optional;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paymnt.dto.PaymntRequestDto;
import com.paymnt.dto.PaymntResponseDto;
import com.paymnt.response.ApiResponse;
import com.paymnt.response.SuccessResponse;
import com.paymnt.service.PaymntService;
import com.paymnt.repository.PaymntRepository;
import com.paymnt.enums.PaymntStatus;

@RestController
@RequestMapping("/paymnt")
public class PaymntController {

	private final PaymntService pay;
	private final PaymntRepository repo;

    private Long parseCustomerId(String value) { try { return value == null || value.isBlank() ? null : Long.valueOf(value); } catch (NumberFormatException e) { return null; } }

	PaymntController(PaymntService pay, PaymntRepository repo) {
		this.pay = pay;
		this.repo = repo;
	}

	@PostMapping("/pay")
	public ResponseEntity<?> initiatePaymnt(@RequestBody PaymntRequestDto request) {

		ApiResponse success = new SuccessResponse<PaymntResponseDto>(pay.makePymnt(request), HttpStatus.CREATED.value(),
				"paymnt successfull", LocalDateTime.now());

		return ResponseEntity.status(200).body(success);

	}

	@PatchMapping("/refund/{id}")

	public String refund(@PathVariable Long id) {

		return pay.refund(id);
	}

	@GetMapping("/paymnt-status/{id}")

	public String getPaymntStatus(@PathVariable Long id) {

		return pay.getPaymntMode(id);
	}


    @GetMapping("/view-all")
    public ResponseEntity<?> viewAll(jakarta.servlet.http.HttpServletRequest request) {
        var payments = repo.findAll();
        if ("CUSTOMER".equals(request.getHeader("X-User-Role"))) {
            Long customerId = parseCustomerId(request.getHeader("X-Customer-Id"));
            payments = payments.stream().filter(p -> customerId != null && customerId.equals(p.getCustomerId())).toList();
        }
        return ResponseEntity.ok(payments.stream().map(p -> PaymntResponseDto.builder()
                .paymentId(p.getPaymentId())
                .bookingId(p.getBookingId())
                .customerId(p.getCustomerId())
                .amount(p.getAmount())
                .paymentMode(p.getPaymentMode())
                .transactionReference(p.getTransactionReference())
                .paymentStatus(p.getPaymentStatus())
                .paymentDate(p.getPaymentDate())
                .build()).toList());
    }

    @GetMapping("/stats/revenue")
    public Double revenue(){ return Optional.ofNullable(repo.totalByStatus(PaymntStatus.COMPLETED)).orElse(0D); }

    @GetMapping("/stats/failed")
    public long failed(){ return repo.countByPaymentStatus(PaymntStatus.FAILED); }
}
