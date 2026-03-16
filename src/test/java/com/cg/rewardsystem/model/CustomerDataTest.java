package com.cg.rewardsystem.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class CustomerDataTest {

    @Test
    void testSettersAndGetters() {
        CustomerData customerData = new CustomerData();
        Long customerId = 101L;
        Map<String, Integer> monthPoints = new HashMap<>();
        monthPoints.put("January", 50);
        monthPoints.put("February", 75);
        int totalPoints = 125;
        customerData.setCustomerId(customerId);
        customerData.setMonth_points(monthPoints);
        customerData.setTotal(totalPoints);
        assertEquals(customerId, customerData.getCustomerId());
        assertEquals(monthPoints, customerData.getMonth_points());
        assertEquals(totalPoints, customerData.getTotal());
    }

    @Test
    void testUpdateValues() {
        CustomerData customerData = new CustomerData();

        customerData.setCustomerId(200L);
        customerData.setTotal(300);
        assertEquals(200L, customerData.getCustomerId());
        assertEquals(300, customerData.getTotal());
        // Update values
        customerData.setCustomerId(250L);
        customerData.setTotal(400);
        assertEquals(250L, customerData.getCustomerId());
        assertEquals(400, customerData.getTotal());
    }

    @Test
    void testMonthPointsManipulation() {
        CustomerData customerData = new CustomerData();
        Map<String, Integer> monthPoints = new HashMap<>();
        customerData.setMonth_points(monthPoints);
        customerData.getMonth_points().put("March", 100);
        customerData.getMonth_points().put("April", 150);
        assertEquals(2, customerData.getMonth_points().size());
        assertEquals(100, customerData.getMonth_points().get("March"));
        assertEquals(150, customerData.getMonth_points().get("April"));
    }
}