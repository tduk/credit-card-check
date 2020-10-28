package com.tarasduk.ap.fraud.detector;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tarasduk.ap.fraud.model.CreditCard;
import com.tarasduk.ap.fraud.model.Transaction;

public class SumPerDaysFraudDetectorTest {
	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
	private static final String TEST_CREDIT_CARD_NUMGER = "1234 1234 1234 1234";
	private CreditCard creditCard;
	
	@BeforeEach
	void beforeEach() {
		creditCard = new CreditCard(TEST_CREDIT_CARD_NUMGER);
	}
	
	@Test
	void shouldNotFailIfSingleTransactionSumLessThan() {
		creditCard.addTransaction(new Transaction("1000", new Date(), 120));

		assertTrue(checkCard(100, 1, creditCard));
	}

	@Test
	void shouldFailIfSingleTransactionSumMoreThan() {
		creditCard.addTransaction(new Transaction("1000", new Date(), 99));

		assertFalse(checkCard(100, 1, creditCard));
	}

	@Test
	void shouldFailIfAllTransactionsSumMoreThan() throws Exception {
		creditCard.addTransaction(createTransaction("2000-01-01T00:00:00", 30));
		creditCard.addTransaction(createTransaction("2000-01-01T00:10:00", 35));

		creditCard.addTransaction(createTransaction("2000-01-01T00:23:00", 35));
		creditCard.addTransaction(createTransaction("2000-01-01T00:23:59", 5));

		assertTrue(checkCard(100, 1, creditCard));
	}

	@Test
	void shouldNotFailIfAllTransactionsSinglePeriodSumLessThan() throws Exception {
		creditCard.addTransaction(createTransaction("2000-01-01T00:00:00", 25));
		creditCard.addTransaction(createTransaction("2000-01-01T00:23:00", 25));
		creditCard.addTransaction(createTransaction("2000-01-01T00:23:59", 35));

		assertFalse(checkCard(100, 1, creditCard));
	}

	@Test
	void shouldNotFailIfAllTransactionsEachPeriodSumLessThan() throws Exception {
		creditCard.addTransaction(createTransaction("2000-01-01T00:00:00", 15));
		creditCard.addTransaction(createTransaction("2000-01-01T10:00:00", 15));
		creditCard.addTransaction(createTransaction("2000-01-02T13:00:00", 60));
		creditCard.addTransaction(createTransaction("2000-01-02T14:11:00", 35));
		creditCard.addTransaction(createTransaction("2000-01-03T23:00:00", 35));
		creditCard.addTransaction(createTransaction("2000-01-03T23:59:00", 64));

		assertFalse(checkCard(100, 1, creditCard));
	}

	@Test
	void shouldFailIfAllTransactionsAnyPeriodSumMoreThan() throws Exception {
		creditCard.addTransaction(createTransaction("2000-01-01T11:00:00", 15));
		creditCard.addTransaction(createTransaction("2000-01-01T12:20:00", 25));
		creditCard.addTransaction(createTransaction("2000-01-02T13:00:00", 70));
		creditCard.addTransaction(createTransaction("2000-01-02T14:11:00", 35));
		creditCard.addTransaction(createTransaction("2000-01-02T22:22:00", 4));
		creditCard.addTransaction(createTransaction("2000-01-03T23:00:00", 35));
		creditCard.addTransaction(createTransaction("2000-01-03T23:59:00", 64));

		assertTrue(checkCard(100, 1, creditCard));
	}
	
	@Test
	void shouldFailIfAllTransactionsAnyLongPeriodSumMoreThan() throws Exception {
		creditCard.addTransaction(createTransaction("2000-01-01T11:00:00", 90));
		creditCard.addTransaction(createTransaction("2000-01-01T12:20:00", 5));
		creditCard.addTransaction(createTransaction("2000-01-02T13:00:00", 10));
		creditCard.addTransaction(createTransaction("2000-01-02T14:11:00", 35));
		creditCard.addTransaction(createTransaction("2000-01-02T22:22:00", 35));
		creditCard.addTransaction(createTransaction("2000-01-03T23:00:00", 35));
		creditCard.addTransaction(createTransaction("2000-01-03T23:59:00", 64));

		assertTrue(checkCard(150, 2, creditCard));
	}
	

	private boolean checkCard(double amount, int period, CreditCard creditCard) {
		SumPerDaysFraudDetector fraudDetector = new SumPerDaysFraudDetector(amount, period);
		return fraudDetector.isCompromised(creditCard);
	}

	private Transaction createTransaction(String date, double amount) throws Exception {
		return new Transaction("1000", createDate(date), amount);
	}

	private Date createDate(String date) throws Exception {
		return dateFormat.parse(date);
	}
}
