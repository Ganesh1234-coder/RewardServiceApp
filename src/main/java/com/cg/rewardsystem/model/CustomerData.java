package com.cg.rewardsystem.model;

import java.util.Map;

/**
* This class processed customer data
*/
public class CustomerData {
	private Long customerId;
	private Map<String, Integer> month_points;
	private int total;
	
	public Long getCustomerId() {
		return customerId;
	}
	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}
	public Map<String, Integer> getMonth_points() {
		return month_points;
	}
	public void setMonth_points(Map<String, Integer> month_points) {
		this.month_points = month_points;
	}
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
}