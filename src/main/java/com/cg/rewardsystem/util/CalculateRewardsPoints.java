package com.cg.rewardsystem.util;

public class CalculateRewardsPoints {

	/**
	 * Calculate reward points based on transaction amount.
	 * @param amount The transaction amount.
	 * @return The calculated reward points.
	 */
	public static int calculatePoints (Double amount) {

		if (amount == null) {
			throw new IllegalArgumentException("amount cannot be null");
		}
		if (amount.isNaN() || amount.isInfinite()) {
			throw new IllegalArgumentException("amount must be a finite number");
		} 
		if (amount < 0) { 
			throw new IllegalArgumentException("amount cannot be negative");
		}
		// Truncate decimal to non decimal (e.g., 100.99 -> 100)
		int dollars=(int) Math.floor(amount);
		if (dollars <= 50) {
			return 0;
		} 
		if (dollars <= 100) { 
			return dollars-50; // 1 point per dollar between 51..100
		} 
		// dollars > 100
		return (dollars-100)*2 + 50; // +50 from the 51..100 tier
	}
}