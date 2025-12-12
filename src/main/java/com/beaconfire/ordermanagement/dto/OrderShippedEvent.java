package com.beaconfire.ordermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author luluxue
 * @date 2025-12-09
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderShippedEvent {
	private String orderId;
	private String trackingNumber;
	private String carrier;
	private LocalDateTime shippedAt;
	private String estimeatedDeliveryDate;
}
