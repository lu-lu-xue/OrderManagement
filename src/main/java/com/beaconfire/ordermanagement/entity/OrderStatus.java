package com.beaconfire.ordermanagement.entity;

/**
 * @author luluxue
 * @date 2025-11-19
 */

public enum OrderStatus {
	PENDING,
	PAYMENT_CONFIRMED,
	CONFIRMED,
	PAYMENT_FAILED,
	INVENTORY_FAILED,
//	PREPARING_SHIPMENT,
	SHIPPED,
	DELIVERED,
	PENDING_CANCELLATION,
	CANCELLED,
	RETURNED,            // return entire order
	PARTIALLY_RETURNED,     //partial return
	PROCESSING
}
