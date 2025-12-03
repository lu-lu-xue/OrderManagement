package com.beaconfire.ordermanagement.entity;

/**
 * @author luluxue
 * @date 2025-11-19
 */

public enum OrderStatus {
	PENDING,
	PROCESSING,
	CONFIRMED,
	PREPARING_SHIPMENT,
	SHIPPED,
	DELIVERED,
	
	CANCELLED,
	RETURNED,
	FAILED
}
