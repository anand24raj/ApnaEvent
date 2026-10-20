package com.customer.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.customer.dto.CustomerRequestDto;
import com.customer.dto.CustomerResponseDto;
import com.customer.response.ApiResponse;
import com.customer.response.SuccessResponse;
import com.customer.service.CustomerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/customer")
public class CustomerController {

	final CustomerService custService;

	CustomerController(CustomerService custService) {
		this.custService = custService;
	}

	@PostMapping("/add")
	public ResponseEntity<?> addCsutomer(@Valid @RequestBody CustomerRequestDto request) {
		ApiResponse response = new SuccessResponse<CustomerResponseDto>(custService.addCustomer(request),
				HttpStatus.CREATED.value(), "customer registered successfully", LocalDateTime.now());

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/view/{id}")
	public ResponseEntity<?> viewCustomer(@PathVariable Long id, jakarta.servlet.http.HttpServletRequest http) {
		if ("CUSTOMER".equals(http.getHeader("X-User-Role")) && !same(id,http)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("message","You can only view your own profile."));

		ApiResponse response = new SuccessResponse<CustomerResponseDto>(custService.viewCustomer(id),
				HttpStatus.CREATED.value(), "customer fetched successfully", LocalDateTime.now());

		return ResponseEntity.status(HttpStatus.OK).body(response);

	}

	@PutMapping("/update/{id}")
	public ResponseEntity<?> updateCustomer(@RequestBody CustomerRequestDto request, @PathVariable Long id, jakarta.servlet.http.HttpServletRequest http) {
		if ("CUSTOMER".equals(http.getHeader("X-User-Role")) && !same(id,http)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("message","You can only update your own profile."));

		ApiResponse response = new SuccessResponse<CustomerResponseDto>(custService.updateCustomer(request, id),
				HttpStatus.OK.value(), "customer updated successfully", LocalDateTime.now());

		return ResponseEntity.status(HttpStatus.OK).body(response);

	}

	@GetMapping("/view-all")
	public ResponseEntity<?> viewAllCustomer() {

		ApiResponse response = new SuccessResponse<List<CustomerResponseDto>>(custService.viewAllCustomer(),
				HttpStatus.OK.value(), "All customer updated successfully", LocalDateTime.now());

		return ResponseEntity.status(HttpStatus.OK).body(response);

	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteCustomer(@PathVariable Long id) {

		ApiResponse response = new SuccessResponse<CustomerResponseDto>(custService.viewCustomer(id),
				HttpStatus.OK.value(), "Customer deleted successfully", LocalDateTime.now());

		custService.deleteCustomer(id);
		return ResponseEntity.status(HttpStatus.OK).body(response);

	}

	private boolean same(Long id, jakarta.servlet.http.HttpServletRequest http){ try{return id!=null&&id.equals(Long.valueOf(http.getHeader("X-Customer-Id")));}catch(Exception e){return false;} }
}
