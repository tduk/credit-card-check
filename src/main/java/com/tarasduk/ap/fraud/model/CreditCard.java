package com.tarasduk.ap.fraud.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class CreditCard {
	@Getter
	private List<Transaction> transactions = new ArrayList<>();

	@Getter
	private String cardNumber;

	@Getter
	@Setter
	private boolean compromised;

	public CreditCard(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public void addTransaction(Transaction transaction) {
		transactions.add(transaction);
	}
}
