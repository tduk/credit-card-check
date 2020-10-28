package com.tarasduk.ap.fraud.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tarasduk.ap.fraud.model.Transaction;

import lombok.Setter;

public class TransactionReader {
	private Logger logger = LoggerFactory.getLogger(TransactionReader.class);

	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

	@Setter
	private String separator = ",";

	@Setter
	private TransactionConsumer transactionConsumer;

	protected Transaction fromString(String str) {
		Transaction transaction = null;

		if (str != null) {
			String[] tokens = str.split(separator);
			if (tokens.length == 3) {
				try {
					transaction = new Transaction(tokens[0], dateFormat.parse(tokens[1]),
							Double.parseDouble(tokens[2]));
				} catch (NumberFormatException e) {
					logger.error("Cannot parse amount in '{}'", str, e);
				} catch (ParseException e) {
					logger.error("Cannot parse date in '{}'", str, e);
				}
			} else {
				logger.error("Cannot parse transaction from '{}'", str);
			}

		}
		return transaction;
	}

	public void readFile(String fileName) throws IOException {
		if (transactionConsumer != null) {
			BufferedReader reader = Files.newBufferedReader(Path.of(fileName));

			reader.lines().forEach(this::processLine);
		}else {
			logger.error("There is no transactionConsumer configured");
		}
	}

	protected void processLine(String line) {
		Transaction transaction = fromString(line);
		if (transaction != null) {
			transactionConsumer.addTransaction(transaction);
		}
	}
}
