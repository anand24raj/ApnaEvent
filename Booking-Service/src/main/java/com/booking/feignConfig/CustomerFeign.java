package com.booking.feignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
@FeignClient("Customer-Service")
public interface CustomerFeign {
 @GetMapping("/customer/view/{id}") ResponseEntity<?> viewCustomer(@PathVariable Long id);
}
