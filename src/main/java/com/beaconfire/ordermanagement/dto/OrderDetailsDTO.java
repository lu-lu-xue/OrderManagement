package com.beaconfire.ordermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.CompletableFuture;

/**
 * @author luluxue
 * @date 2025-12-31
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailsDTO {
	private OrderResponseDTO response;
	private PaymentResponseDTO payment;
	private ShipmentResponseDTO shipment;
}
