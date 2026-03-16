package com.cg.rewardsystem.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cg.rewardsystem.entity.Transaction;

public interface TranscationRepository extends JpaRepository<Transaction, Long> {

	/**
	 *	Fetch transaction for a given customer within a specified date range.
	 *		@param customerId The ID of the customer.
	 *		@param startbate The start date of the range.
	 *		@param endDate The end date of the range.
	 *		@return List of Transaction within the range.
	 */
	@Query("SELECT t FROM Transaction t WHERE t.customerId = :customerId AND t.transactionDate >= :startDate AND t.transactionDate < :endDate") 
	List<Transaction> findByCustomerIdAndDateBetween(@Param("customerId") Long customerId, @Param("startDate")LocalDate startDate,
	@Param("endDate")LocalDate endDate);
}
