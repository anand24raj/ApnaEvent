package com.booking.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.springframework.stereotype.Service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

import com.booking.dto.PaymntRequestDto;
import com.booking.dto.PaymntResponseDto;
import com.booking.feignConfig.PaymntFeign;
import com.booking.response.SuccessResponse;

@Service
public class PaymentClientService {
    private final PaymntFeign paymentFeign;

    public PaymentClientService(PaymntFeign paymentFeign) {
        this.paymentFeign = paymentFeign;
    }

    @CircuitBreaker(name = "paymentService", fallbackMethod = "fallback")
    @Retry(name = "paymentService")
    @TimeLimiter(name = "paymentService", fallbackMethod = "fallback")
    
    public CompletableFuture<SuccessResponse<PaymntResponseDto>> pay(PaymntRequestDto request) {
        return CompletableFuture.supplyAsync(() -> {
            var response = paymentFeign.savePaymnt(request);
            if (response == null || response.getBody() == null) 
            {
                throw new IllegalStateException("Payment Service returned an empty response");
            }
            return response.getBody();
        });
    }

    private CompletableFuture<SuccessResponse<PaymntResponseDto>> fallback(PaymntRequestDto request, Throwable ex) {
        String message = ex instanceof CompletionException && ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
        return CompletableFuture.failedFuture(new IllegalStateException("Payment Service is temporarily unavailable: " + message, ex));
    }

    public String refund(Long paymentId) 
    {
        return paymentFeign.refund(paymentId);
    }
}
