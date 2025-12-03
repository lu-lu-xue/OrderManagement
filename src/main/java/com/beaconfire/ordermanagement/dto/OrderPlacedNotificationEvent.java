package com.beaconfire.ordermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author luluxue
 * @date 2025-11-30
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderPlacedNotificationEvent {
	private String orderId;
	private String userId;
	private BigDecimal totalAmount;
	private LocalDateTime createdAt;
	private String orderDetailsURL;
}
