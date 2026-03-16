package com.cg.rewardsystem.service;

import org.springframework.http.ResponseEntity;

import com.cg.rewardsystem.model.CustomerData;

public interface IRewardService {

	  ResponseEntity<CustomerData> getRewardByCustomerResponse(Long customerId);

}