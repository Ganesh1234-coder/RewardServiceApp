package com.cg.rewardsystem.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="TRANSACTIONS")
public class Transaction {

	@Id
	private Long id;
	private Long customerId;
	private Double amount;
	private LocalDate transactionDate;
	
	public Transaction() {
		super();
	}
	public Transaction(Long id, Long customerId, Double amount, LocalDate transactionDate) {
		super();
		this.id = id;
		this.customerId = customerId;
		this.amount = amount;
		this.transactionDate = transactionDate;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getCustomerId() {
		return customerId;
	}
	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}
	public Double getAmount() {
		return amount;
	}
	public void setAmount(Double amount) {
		this.amount = amount;
	}
	public LocalDate getTransactionDate() {
		return transactionDate;
	}
	public void setTransactionDate(LocalDate transactionDate) {
		this.transactionDate = transactionDate;
	}
}