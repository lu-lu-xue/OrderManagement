package com.beaconfire.ordermanagement.exception;

/**
 * @author luluxue
 * @date 2025-12-02
 */
public class PaymentFailureException extends RuntimeException{
	private final String paymentStatus;
	
	public PaymentFailureException(String message, String paymentStatus){
		super(message);
		this.paymentStatus = paymentStatus;
	}
}
