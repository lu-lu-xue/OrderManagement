package com.beaconfire.ordermanagement.entity;

/**
 * @author luluxue
 * @date 2025-11-19
 */

public enum OrderStatus {
	PENDING,
	CONFIRMED,
	PAYMENT_FAILED,
	INVENTORY_FAILED,
//	PREPARING_SHIPMENT,
	SHIPPED,
	DELIVERED,
	CANCELLED,
	RETURNED,               // return entire order
	PARTIALLY_RETURNED,     //partial return
	PROCESSING
}
