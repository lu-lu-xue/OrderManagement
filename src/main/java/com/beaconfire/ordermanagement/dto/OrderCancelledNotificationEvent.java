package com.beaconfire.ordermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author luluxue
 * @date 2025-12-05
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderCancelledNotificationEvent {
	private String orderId;
	private String userId;
	private BigDecimal totalAmount;
	private LocalDateTime cancelledAt;
	private String orderDetailsURL;
}
