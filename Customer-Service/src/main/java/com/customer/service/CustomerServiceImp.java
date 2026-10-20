package com.customer.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.customer.customexception.CustomerAlreadyExistsException;
import com.customer.customexception.CustomerNotFoundException;
import com.customer.dto.CustomerRequestDto;
import com.customer.dto.CustomerResponseDto;
import com.customer.entity.Customer;
import com.customer.enums.MemberShip;
import com.customer.enums.Status;
import com.customer.repository.CustomerRepository;

@Service
public class CustomerServiceImp implements CustomerService {

	private final CustomerRepository custRepo;

	public CustomerServiceImp(CustomerRepository custRepo) {
		this.custRepo = custRepo;
	}

	@Override
	@Transactional
	public CustomerResponseDto addCustomer(CustomerRequestDto request) {
		String email = request.getEmail().trim().toLowerCase();
		String mobile = request.getMobile().trim();

		if (custRepo.existsByEmailIgnoreCase(email)) {
			throw new CustomerAlreadyExistsException("Customer already exists with email: " + email);
		}
		if (custRepo.existsByMobile(mobile)) {
			throw new CustomerAlreadyExistsException("Customer already exists with mobile: " + mobile);
		}

		Customer customer = new Customer();
		BeanUtils.copyProperties(request, customer);
		customer.setEmail(email);
		customer.setMobile(mobile);
		customer.setMemberShipType(MemberShip.REGULAR);
		customer.setStatus(Status.ACTIVE);

		Customer saved = custRepo.save(customer);

		return toResponse(saved);
	}

	@Override
	public CustomerResponseDto viewCustomer(Long id) {
		return toResponse(
				custRepo.findById(id).orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + id)));
	}

	@Override
	public List<CustomerResponseDto> viewAllCustomer() {
		List<CustomerResponseDto> result = new ArrayList<>();
		for (Customer customer : custRepo.findAll()) {
			result.add(toResponse(customer));
		}
		return result;
	}

	@Override
	public String deleteCustomer(Long id) {
		Customer customer = custRepo.findById(id)
				.orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + id));
		custRepo.delete(customer);
		return "Customer deleted having id: " + id;
	}

	@Override
	@Transactional
	public CustomerResponseDto updateCustomer(CustomerRequestDto request, long id) {
		Customer customer = custRepo.findById(id)
				.orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + id));

		String email = request.getEmail().trim().toLowerCase();
		String mobile = request.getMobile().trim();

		if (!email.equalsIgnoreCase(customer.getEmail()) && custRepo.existsByEmailIgnoreCase(email)) {
			throw new CustomerAlreadyExistsException("Email already exists: " + email);
		}
		if (!mobile.equals(customer.getMobile()) && custRepo.existsByMobile(mobile)) {
			throw new CustomerAlreadyExistsException("Mobile already exists: " + mobile);
		}

		BeanUtils.copyProperties(request, customer);
		customer.setCustommerId(id);
		customer.setEmail(email);
		customer.setMobile(mobile);
		return toResponse(custRepo.save(customer));
	}

	private CustomerResponseDto toResponse(Customer customer) {
		CustomerResponseDto response = new CustomerResponseDto();
		BeanUtils.copyProperties(customer, response);
		return response;
	}
}
