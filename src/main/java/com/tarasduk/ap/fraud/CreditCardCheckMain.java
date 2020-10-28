package com.tarasduk.ap.fraud;

import java.io.IOException;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tarasduk.ap.fraud.detector.FraudDetector;
import com.tarasduk.ap.fraud.detector.SumPerDaysFraudDetector;
import com.tarasduk.ap.fraud.utils.TransactionReader;

public class CreditCardCheckMain {
	private static final int PERIOD_IN_DAYS = 1;

	private Logger log = LoggerFactory.getLogger(CreditCardCheckMain.class);

	private TransactionProcessingManager mgr;

	private TransactionReader transactionReader;

	void init(TransactionProcessingManager mgr, TransactionReader reader, FraudDetector... fraudDetectors) {
		this.mgr = mgr;
		this.transactionReader = reader;
		this.transactionReader.setTransactionConsumer(mgr);
		
		Stream.of(fraudDetectors).forEach(mgr::addFraudDetector);
	}

	public void run(String fileName) {
		try {
			transactionReader.readFile(fileName);
		} catch (IOException e) {
			log.error("Unable to read a file '{}'", fileName, e);
			return;
		}
		mgr.analyse();
	}

	public void printResults() {
		mgr.getCreditCards().values().stream().filter(report -> report.isCompromised())
				.map(report -> report.getCardNumber()).forEach(System.out::println);

	}

	public static void main(String... args) {
		CreditCardCheckMain main = new CreditCardCheckMain();

		if (args.length != 2) {
			System.err.println("Error: please provide parameters <filename> <sumThreshold>");
			return;
		}

		try {
			Double sumThreshold = Double.parseDouble(args[1]);
			
			TransactionProcessingManager mgr = new TransactionProcessingManager();
			TransactionReader transactionReader = new TransactionReader();
			FraudDetector sumPerDaysDetector = new SumPerDaysFraudDetector(sumThreshold, PERIOD_IN_DAYS);
			
			main.init(mgr, transactionReader, sumPerDaysDetector);
		} catch (NumberFormatException ex) {
			System.err.println("Error: unable to parse <sumThreshold> parameter");
			return;
		}
		
		main.run(args[0]);

		main.printResults();
	}
}
