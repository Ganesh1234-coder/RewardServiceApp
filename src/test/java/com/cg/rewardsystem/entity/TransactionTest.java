package com.cg.rewardsystem.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class TransactionTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setCustomerId(100L);
        transaction.setAmount(250.75);
        transaction.setTransactionDate(LocalDate.of(2024, 5, 20));

        assertEquals(1L, transaction.getId());
        assertEquals(100L, transaction.getCustomerId());
        assertEquals(250.75, transaction.getAmount());
        assertEquals(LocalDate.of(2024, 5, 20), transaction.getTransactionDate());
    }
    @Test
    void testAllArgsConstructor() {
        LocalDate date = LocalDate.of(2024, 6, 15);
        Transaction transaction = new Transaction(2L, 200L, 500.0, date);

        assertEquals(2L, transaction.getId());
        assertEquals(200L, transaction.getCustomerId());
        assertEquals(500.0, transaction.getAmount());
        assertEquals(date, transaction.getTransactionDate());
    }

    @Test
    void testUpdateValues() {
        Transaction transaction = new Transaction(3L, 300L, 1000.0, LocalDate.of(2024, 7, 10));

        transaction.setAmount(1200.0);
        transaction.setCustomerId(350L);

        assertEquals(1200.0, transaction.getAmount());
        assertEquals(350L, transaction.getCustomerId());
    }
}
