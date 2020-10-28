package com.tarasduk.ap.fraud.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import com.tarasduk.ap.fraud.model.Transaction;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;



@ExtendWith(MockitoExtension.class)
class TransactionReaderTest {
	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

	@Mock
	TransactionConsumer consumer;
	
	TransactionReader transactionReader;
	
	ListAppender<ILoggingEvent> listAppender;
	
	@BeforeEach
	void beforeEach() {
		listAppender = new ListAppender<>();
		listAppender.start();

		Logger logger =  (Logger) LoggerFactory.getLogger(TransactionReader.class);
		logger.addAppender(listAppender);

		transactionReader = new TransactionReader();
		transactionReader.setTransactionConsumer(consumer);
	}
	
	@Test
	void shouldBeNullIfHasLessThan3Tokens() {
		assertNull(transactionReader.fromString("123"));
		assertEquals(1, listAppender.list.size());
		assertEquals("Cannot parse transaction from '123'", listAppender.list.get(0).getFormattedMessage());
	}
	
	@Test
	void shouldBeNullIfStringHasMoreThan3Tokens() {
		assertNull(transactionReader.fromString("123,123,123,123"));
		assertEquals(1, listAppender.list.size());
		assertEquals("Cannot parse transaction from '123,123,123,123'", listAppender.list.get(0).getFormattedMessage());
	}

	@Test
	void shouldBeNullIfStringIsNull() {
		assertNull(transactionReader.fromString(null));
	}

	@Test
	void shouldBeNullIfDateUnparsable() {
		assertNull(transactionReader.fromString("123,123,123"));
		assertEquals(1, listAppender.list.size());
		assertEquals("Cannot parse date in '123,123,123'", listAppender.list.get(0).getFormattedMessage());
	}
	
	@Test
	void shouldBeNullIfAmountUnparsable() {
		assertNull(transactionReader.fromString("123,2000-10-10T10:10:10,test"));
		assertEquals(1, listAppender.list.size());
		assertEquals("Cannot parse amount in '123,2000-10-10T10:10:10,test'", listAppender.list.get(0).getFormattedMessage());
	}
	
	@Test
	void shoudReturnTransaction() {
		Transaction transaction = transactionReader.fromString("123,2000-10-10T10:10:10,123.22");
		
		assertNotNull(transaction);
		assertEquals("123", transaction.getCardNumber());
		assertEquals("2000-10-10T10:10:10", dateFormat.format(transaction.getDate()));
		assertEquals(123.22, transaction.getAmount());
	}
	
	@Test
	void shouldAddNotNullTransactions() {
		transactionReader.processLine("123,2000-10-10T10:10:10,123.22");
		Mockito.verify(consumer).addTransaction(Mockito.any());
	}
	
	@Test
	void shouldNotAddNullTransactions() {
		transactionReader.processLine("123");
		Mockito.verify(consumer, Mockito.never()).addTransaction(Mockito.any());
	}
	
	@Test
	void shouldNotReadFileIfNoConsumer() throws IOException {
		transactionReader.setTransactionConsumer(null);
		transactionReader.readFile("test");
		assertEquals(1, listAppender.list.size());
		assertEquals("There is no transactionConsumer configured", listAppender.list.get(0).getFormattedMessage());
	}
	
	@Test
	void shouldReadAllTheLinesInAFile() throws Exception {
		URI fileUri  = getClass().getClassLoader().getResource("test-valid.csv").toURI();
		TransactionReader spyReader = Mockito.spy(transactionReader);
		
		spyReader.readFile(Paths.get(fileUri).toString());
		
		Mockito.verify(spyReader, Mockito.times(2)).processLine(Mockito.anyString());
	}
}
