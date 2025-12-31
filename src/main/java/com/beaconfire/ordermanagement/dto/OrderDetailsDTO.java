package com.beaconfire.ordermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author luluxue
 * @date 2025-12-31
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailsDTO {
	// from DB: order info
	private OrderResponseDTO order;
	// from paymentService, transactionId, status...
	private PaymentResponseDTO payment;
	// from shipmentService, trackingNumber, carrier...
	private ShipmentResponseDTO shipment;
}
