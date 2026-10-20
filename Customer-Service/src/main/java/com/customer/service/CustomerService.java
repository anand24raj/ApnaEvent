package com.customer.service;

import java.util.List;

import com.customer.dto.CustomerRequestDto;
import com.customer.dto.CustomerResponseDto;

public interface CustomerService {

	public CustomerResponseDto addCustomer(CustomerRequestDto customer);

	public CustomerResponseDto viewCustomer(Long id);

	public List<CustomerResponseDto> viewAllCustomer();

	public String deleteCustomer(Long id);

	public CustomerResponseDto updateCustomer(CustomerRequestDto customer, long id);

}
