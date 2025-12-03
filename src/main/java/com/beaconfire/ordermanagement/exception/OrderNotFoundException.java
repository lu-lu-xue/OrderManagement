package com.beaconfire.ordermanagement.exception;

/**
 * @author luluxue
 * @date 2025-11-24
 */
public class OrderNotFoundException extends RuntimeException{
	public OrderNotFoundException(String message){
		super(message);
	}
}
