package com.cg.rewardsystem.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.cg.rewardsystem.model.CustomerData;
import com.cg.rewardsystem.service.IRewardService;


@WebMvcTest(RewardController.class)
class RewardControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private IRewardService service;

	@Test
	void getRewardsResponse_returnsDatawhenServiceReturnsok() throws Exception {

		CustomerData mockCustomer = new CustomerData();
		mockCustomer.setCustomerId(1L);
		mockCustomer.setTotal (140);

		ResponseEntity<CustomerData> response = ResponseEntity.ok(mockCustomer);
		Mockito.when(service.getRewardByCustomerResponse (1L)).thenReturn (response);
		mvc.perform(MockMvcRequestBuilders.get("/rewards/1"))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.customerId").value(1))
		.andExpect(jsonPath("$.total").value (140));
		

	}

	@Test
	void getRewardsResponse_returns404_whenServiceReturnsNotFound() throws Exception {

		Mockito.when(service.getRewardByCustomerResponse(999L)).thenReturn (ResponseEntity.notFound().build());
		mvc.perform(MockMvcRequestBuilders.get("/rewards/999"))

		.andExpect(status().isNotFound());
	}

}