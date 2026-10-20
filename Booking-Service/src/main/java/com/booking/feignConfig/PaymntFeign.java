package com.booking.feignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.booking.dto.PaymntRequestDto;
import com.booking.dto.PaymntResponseDto;
import com.booking.response.SuccessResponse;
@FeignClient("Paymnt-Service")
public interface PaymntFeign {
 @PostMapping("/paymnt/pay") ResponseEntity<SuccessResponse<PaymntResponseDto>> savePaymnt(@RequestBody PaymntRequestDto request);
 @PatchMapping("/paymnt/refund/{id}") String refund(@PathVariable Long id);
 @GetMapping("/paymnt/paymnt-status/{id}") String getPaymntMode(@PathVariable Long id);
}
