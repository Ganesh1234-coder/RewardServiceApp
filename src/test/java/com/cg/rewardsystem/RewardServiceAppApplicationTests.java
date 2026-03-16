package com.cg.rewardsystem;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.cg.rewardsystem.service.IRewardService;



@SpringBootTest
class RewardServiceAppApplicationTests {
	
	@Autowired
	private IRewardService rewardService;

	
	@Test
	void contextLoads() {
		assertNotNull(rewardService);
	}
	
}

