package com.beaconfire.ordermanagement.exception;

/**
 * @author luluxue
 * @date 2025-12-02
 */
public class PaymentServiceUnavailableException extends RuntimeException{
	
	public PaymentServiceUnavailableException(String message, Throwable cause){
		super(message, cause);
	}
}
