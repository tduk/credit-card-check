package com.tarasduk.ap.fraud.detector;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.tarasduk.ap.fraud.model.CreditCard;
import com.tarasduk.ap.fraud.model.Transaction;

public class SumPerDaysFraudDetector implements FraudDetector{
	private double amount;
	private long periodInMillis;
	
	public SumPerDaysFraudDetector(double amount, int days) {
		this.amount  = amount;
		this.periodInMillis = TimeUnit.DAYS.toMillis(days);
				
	}
	@Override
	public boolean isCompromised(CreditCard creditCard) {
		List<Transaction> transactions = creditCard.getTransactions();

		for(int i=0; i < transactions.size();i++) {
			if(isCompromisedInSinglePeriod(i, transactions)) {
				return true;
			}
		}
	
		return false;
	}
	
	private boolean isCompromisedInSinglePeriod(int index, List<Transaction> transactions) {
		long periodStart = transactions.get(index).getDate().getTime();
		
		long currentPeriod = 0;
		
		double totalSum = 0;
		
		for(int i=index; i<transactions.size(); i++) {
			Transaction current = transactions.get(i);
			totalSum += current.getAmount();
			
			currentPeriod = current.getDate().getTime() - periodStart;
			if( currentPeriod > periodInMillis) {
				break;
			}
			if(totalSum > amount) {
				return true;
			}
		}
		return false;
		
	}
}
