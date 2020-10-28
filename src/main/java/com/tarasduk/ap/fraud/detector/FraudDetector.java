package com.tarasduk.ap.fraud.detector;

import com.tarasduk.ap.fraud.model.CreditCard;

public interface FraudDetector {
	boolean isCompromised(CreditCard creditCard);
}
