package com.tarasduk.ap.fraud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import com.tarasduk.ap.fraud.detector.FraudDetector;
import com.tarasduk.ap.fraud.model.CreditCard;
import com.tarasduk.ap.fraud.utils.TransactionReader;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

class CreditCardCheckMainTest {
	private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
	private final PrintStream originalErr = System.err;

	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final PrintStream originalOut = System.err;

	
	TransactionProcessingManager mgr;
	TransactionReader reader;
	FraudDetector fraudDetector;

	CreditCardCheckMain main;
	ListAppender<ILoggingEvent> listAppender;


	@BeforeEach
	void beforeEach() {
		mgr = Mockito.mock(TransactionProcessingManager.class);
		reader = Mockito.mock(TransactionReader.class);
		fraudDetector = Mockito.mock(FraudDetector.class);

		main = new CreditCardCheckMain();

		listAppender = new ListAppender<>();
		listAppender.start();

		Logger logger =  (Logger) LoggerFactory.getLogger(CreditCardCheckMain.class);
		logger.addAppender(listAppender);

		System.setErr(new PrintStream(errContent));
		System.setOut(new PrintStream(outContent));
	}

	@Test
	void shoudFailIfMoreThan2Params() {
		CreditCardCheckMain.main("File", "20.20", "test");
		assertTrue(errContent.toString().contains("Error: please provide parameters <filename> <sumThreshold>"));
	}

	@Test
	void shoudFailIfLessThan2Params() {
		CreditCardCheckMain.main("File");
		assertTrue(errContent.toString().contains("Error: please provide parameters <filename> <sumThreshold>"));
	}
	
	@Test
	void shoudFailIfAmountIsInvalid() {
		CreditCardCheckMain.main("File","test");
		assertTrue(errContent.toString().contains("Error: unable to parse <sumThreshold> parameter"));
	}

	@Test
	void testInit() {
		main.init(mgr, reader, fraudDetector);
		
		Mockito.verify(reader).setTransactionConsumer(mgr);
		Mockito.verify(mgr).addFraudDetector(fraudDetector);
	}

	@Test
	void shouldNotRunOnException() throws IOException {
		Mockito.doThrow(new IOException()).when(reader).readFile(Mockito.anyString());

		main.init(mgr, reader);
		main.run("test");
		
		assertEquals("Unable to read a file 'test'", listAppender.list.get(0).getFormattedMessage());
		Mockito.verify(mgr, Mockito.never()).analyse();
	}

	@Test
	void shouldNotRunOnSuccess() throws IOException {
		main.init(mgr, reader);
		main.run("test");
		
		Mockito.verify(mgr).analyse();
	}
	
	@Test
	void testPrintResult() {
		main.init(mgr, reader);
		
		Map<String, CreditCard> map= new HashMap<>();
		map.put("valid", new CreditCard("valid"));
		
		CreditCard invalid = new CreditCard("invalid");
		invalid.setCompromised(true);
		
		map.put("invalid", invalid);
		
		Mockito.when(mgr.getCreditCards()).thenReturn(map);
		main.printResults();
		assertEquals("invalid\n", outContent.toString());
	}

	
	@AfterEach
	void afterEach() {
		System.setErr(originalErr);
		System.setOut(originalOut);
	}
}
