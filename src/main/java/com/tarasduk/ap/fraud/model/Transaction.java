package com.tarasduk.ap.fraud.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Transaction {
	private String cardNumber;
	private Date date;
	private double amount;
}
