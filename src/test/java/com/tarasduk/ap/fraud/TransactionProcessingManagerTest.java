package com.tarasduk.ap.fraud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.tarasduk.ap.fraud.detector.FraudDetector;
import com.tarasduk.ap.fraud.model.CreditCard;
import com.tarasduk.ap.fraud.model.Transaction;

class TransactionProcessingManagerTest {
	private TransactionProcessingManager mgr;
	
	
	@BeforeEach
	void beforeEach() {
		mgr = new TransactionProcessingManager();
	}
	
	@Test
	void shouldCreateNewCreditCardIfNotExist() {
		Transaction transaction = new Transaction("TEST", new Date(), 10);
		mgr.addTransaction(transaction);

		assertTrue(mgr.getCreditCards().containsKey("TEST"));
		assertEquals(transaction, mgr.getCreditCards().get("TEST").getTransactions().get(0));
	}
	
	@Test
	void shouldAddToExistingCreditCard() {
		mgr.getCreditCards().put("TEST", new CreditCard("TEST"));
		
		assertTrue(mgr.getCreditCards().containsKey("TEST"));
		assertTrue(mgr.getCreditCards().get("TEST").getTransactions().isEmpty());
		
		Transaction transaction = new Transaction("TEST", new Date(), 10);
		mgr.addTransaction(transaction);

		assertEquals(transaction, mgr.getCreditCards().get("TEST").getTransactions().get(0));
	}
	
	@Test
	void shouldAnalyseEachCreditCard() {
		TransactionProcessingManager spyMgr = Mockito.spy(mgr);
		CreditCard creditCard1 = new CreditCard("TEST1");
		CreditCard creditCard2 = new CreditCard("TEST2");
		
		mgr.getCreditCards().put("TEST1", creditCard1);
		mgr.getCreditCards().put("TEST2", creditCard2);
		
		spyMgr.analyse();
		
		Mockito.verify(spyMgr).analyse(creditCard1);
		Mockito.verify(spyMgr).analyse(creditCard2);
	}
	
	@Test
	void shouldUseEachDeterctor() {
		FraudDetector fraudDetector1 = Mockito.mock(FraudDetector.class);
		FraudDetector fraudDetector2 = Mockito.mock(FraudDetector.class);

		mgr.addFraudDetector(fraudDetector1);
		mgr.addFraudDetector(fraudDetector2);
		
		
		mgr.analyse(new CreditCard("TEST"));
		
		Mockito.verify(fraudDetector1).isCompromised(Mockito.any());
		Mockito.verify(fraudDetector2).isCompromised(Mockito.any());
	}
	
	@Test
	void shouldSetCompromisedOnAnyDetectorsMatch() {
		FraudDetector fraudDetector1 = Mockito.mock(FraudDetector.class);
		FraudDetector fraudDetector2 = Mockito.mock(FraudDetector.class);
		mgr.addFraudDetector(fraudDetector1);
		mgr.addFraudDetector(fraudDetector2);

		Mockito.when(fraudDetector1.isCompromised(Mockito.any())).thenReturn(true);
		Mockito.when(fraudDetector2.isCompromised(Mockito.any())).thenReturn(false);
		
		CreditCard creditCard = new CreditCard("TEST");
		mgr.analyse(creditCard);
		
		assertTrue(creditCard.isCompromised());
	}
	
	@Test
	void shouldStopAnalysingIfCompromised() {
		FraudDetector fraudDetector1 = Mockito.mock(FraudDetector.class);
		FraudDetector fraudDetector2 = Mockito.mock(FraudDetector.class);
		mgr.addFraudDetector(fraudDetector1);
		mgr.addFraudDetector(fraudDetector2);

		Mockito.when(fraudDetector1.isCompromised(Mockito.any())).thenReturn(true);
		Mockito.when(fraudDetector2.isCompromised(Mockito.any())).thenReturn(false);
		
		mgr.analyse(new CreditCard("TEST"));
		
		Mockito.verify(fraudDetector1).isCompromised(Mockito.any());
		Mockito.verify(fraudDetector2, Mockito.times(0)).isCompromised(Mockito.any());
	}

	

}
