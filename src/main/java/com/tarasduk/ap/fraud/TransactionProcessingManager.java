package com.tarasduk.ap.fraud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tarasduk.ap.fraud.detector.FraudDetector;
import com.tarasduk.ap.fraud.model.CreditCard;
import com.tarasduk.ap.fraud.model.Transaction;
import com.tarasduk.ap.fraud.utils.TransactionConsumer;

import lombok.Getter;

public class TransactionProcessingManager implements TransactionConsumer{
	@Getter
	private Map<String, CreditCard> creditCards = new HashMap<>();

	private List<FraudDetector> fraudDetectors = new ArrayList<>();

	public void addFraudDetector(FraudDetector fraudDetector) {
		this.fraudDetectors.add(fraudDetector);
	}
	
	public void addTransaction(Transaction transaction) {
		String cardNumber = transaction.getCardNumber();

		if (!creditCards.containsKey(cardNumber)) {
			creditCards.put(cardNumber, new CreditCard(cardNumber));
		}

		CreditCard creditCard = creditCards.get(cardNumber);

		creditCard.addTransaction(transaction);
	}
	
	public void analyse() {
		creditCards.values().stream().forEach(creditCard -> analyse(creditCard));
	}
	
	public void analyse(CreditCard creditCard) {
		creditCard.setCompromised(fraudDetectors.stream().anyMatch(detector->detector.isCompromised(creditCard)));
	}
}
