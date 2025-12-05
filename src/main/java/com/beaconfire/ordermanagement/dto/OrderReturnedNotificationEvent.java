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
public class OrderReturnedNotificationEvent {
	private String orderId;
	private String userId;   // lookup user email
	private BigDecimal refundAmount;
	private String returnReason;
	private boolean isFullReturn;
	private LocalDateTime returnedAt;
}
