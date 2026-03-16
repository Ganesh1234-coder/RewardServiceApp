package com.cg.rewardsystem.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CalculateRewardsPointsTest {

    @Test
    void testAmountNullThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> CalculateRewardsPoints.calculatePoints(null),
                "amount cannot be null");
    }

    @Test
    void testAmountNaNThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> CalculateRewardsPoints.calculatePoints(Double.NaN),
                "amount must be a finite number");
    }

    @Test
    void testAmountInfiniteThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> CalculateRewardsPoints.calculatePoints(Double.POSITIVE_INFINITY),
                "amount must be a finite number");
    }

    @Test
    void testNegativeAmountThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> CalculateRewardsPoints.calculatePoints(-10.0),
                "amount cannot be negative");
    }

    @Test
    void testAmountLessThanOrEqual50() {
        assertEquals(0, CalculateRewardsPoints.calculatePoints(50.0));
        assertEquals(0, CalculateRewardsPoints.calculatePoints(25.99));
    }

    @Test
    void testAmountBetween51And100() {
        assertEquals(1, CalculateRewardsPoints.calculatePoints(51.0));
        assertEquals(49, CalculateRewardsPoints.calculatePoints(99.99));
        assertEquals(50, CalculateRewardsPoints.calculatePoints(100.0));
    }

    @Test
    void testAmountGreaterThan100() {
        assertEquals(52, CalculateRewardsPoints.calculatePoints(101.0));
        assertEquals(70, CalculateRewardsPoints.calculatePoints(110.0));
    }

    @Test
    void testDecimalTruncation() {
        // 100.99 should truncate to 100 → 50 points
        assertEquals(50, CalculateRewardsPoints.calculatePoints(100.99));
        // 150.75 truncates to 150 → (150-100)*2 + 50 = 150 points
        assertEquals(150, CalculateRewardsPoints.calculatePoints(150.75));
    }
}