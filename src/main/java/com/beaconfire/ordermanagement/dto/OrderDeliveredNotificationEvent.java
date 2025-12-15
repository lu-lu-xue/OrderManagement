package com.beaconfire.ordermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author luluxue
 * @date 2025-12-15
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDeliveredNotificationEvent {
	private String orderId;
	private String userId;  // lookup email for the user
	private LocalDateTime deliveredAt;
}
