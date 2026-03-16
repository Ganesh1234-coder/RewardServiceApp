package com.cg.rewardsystem.service;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cg.rewardsystem.entity.Transaction;
import com.cg.rewardsystem.exception.DataNotFoundException;
import com.cg.rewardsystem.model.CustomerData;
import com.cg.rewardsystem.repository.TranscationRepository;
import com.cg.rewardsystem.util.CalculateRewardsPoints;

@Service
public class RewardServiceImpl implements IRewardService{

	private Logger log=LoggerFactory.getLogger(RewardServiceImpl.class);

	@Autowired
	private TranscationRepository transcationRepository;

	/**
	 * Retrieve reward points for a given customer over the last three months.
	 * @param customerId The ID of the customer.
	 * @return A map of months and corresponding reward points.
	 * 
	 */

	@Override
	public ResponseEntity<CustomerData> getRewardByCustomerResponse(Long customerId) {

		final Map<String, Integer> monthData = new LinkedHashMap<>();
		final CustomerData customerData = new CustomerData();
		final LocalDate today=LocalDate.now();

		// anchor is TODAY (exclusive upper bound of the newest monthPeriod)
		LocalDate tDate = today;
		// Build the 3 monthPeriod from OLDEST > NEWEST so JSON is in time order
		// monthPeriod: [month-3, month-2), [month-2, month-1), [month-1, month)
		List<LocalDate[]> monthPeriod = new ArrayList<>(3);
		
		for (int i=3;i >= 1;i--) {
			LocalDate start=tDate.minusMonths(i);
			LocalDate end = tDate.minusMonths (i - 1);
			monthPeriod.add(new LocalDate[]{start, end});
		}

		try {
			customerData.setCustomerId(customerId);
			for (LocalDate[] mon : monthPeriod) {
				LocalDate start = mon[0];
				LocalDate end = mon [1];
				log.info("Window: start={} | end={} (month-open [start, end))", start, end);
				List<Transaction> transactions=transcationRepository.findByCustomerIdAndDateBetween(customerId, start, end);
				transactions.forEach(val->log.info(
						"DB Row -> Id={}, CustomerId={}, Amount={}, Date={}", val.getId(),val.getCustomerId(),val.getAmount(),val.getTransactionDate()
						)); 
				int totalPoints=transactions.stream().mapToInt(t -> CalculateRewardsPoints.calculatePoints(t.getAmount())).peek(p-> log.info("points for tx: {}", p)).sum();
				// Label like "07-02-2026 to 07-03-2026"
				String label = String.format(
						"%02d-%02d-%04d to %02d-%02d-%04d", 
						start.getDayOfMonth(), start.getMonthValue(), start.getYear(), 
						end.getDayOfMonth(), end.getMonthValue(), end.getYear());
				monthData.put(label, totalPoints);
			}
			int sum = monthData.values().stream().mapToInt(Integer::intValue).sum();
			customerData.setMonth_points(monthData); 
			customerData.setTotal(sum); 
			return ResponseEntity.ok(customerData);
		} catch (Exception e) {
			log.error("Failed to compute rewards for customerId={}", customerId, e);
			throw new DataNotFoundException("Data is not found for mentioned customer id.");
		}
	}
}
