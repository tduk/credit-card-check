package com.tarasduk.ap.fraud.utils;

import com.tarasduk.ap.fraud.model.Transaction;

public interface TransactionConsumer {
	void addTransaction(Transaction transaction);
}
