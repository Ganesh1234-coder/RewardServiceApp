package com.cg.rewardsystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cg.rewardsystem.model.CustomerData;
import com.cg.rewardsystem.service.IRewardService;

@RestController
@RequestMapping("/rewards")
public class RewardController {

	@Autowired
	private IRewardService service;

	/**
	 * Retrieve reward points for a given customers.
	 * @param customerId The ID of the customer.
	 * @return Customer Data with total and 3 month rewards points.
	 */
	@GetMapping("/{customerId}")
	public ResponseEntity<CustomerData> getRewardsResponse(@PathVariable Long customerId) {
		return service.getRewardByCustomerResponse(customerId);
		
	}

}