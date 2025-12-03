package com.beaconfire.ordermanagement.exception;

/**
 * @author luluxue
 * @date 2025-11-23
 */
public class ProductNotFoundException extends RuntimeException{
	public ProductNotFoundException(String message){
		super(message);
	}
}
