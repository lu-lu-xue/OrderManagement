package com.beaconfire.ordermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author luluxue
 * @date 2025-12-14
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderConfirmedNotificationEvent {
	private String orderId;
	private String userId;
	private BigDecimal totalAmount;
	private LocalDateTime confirmedAt;
}
