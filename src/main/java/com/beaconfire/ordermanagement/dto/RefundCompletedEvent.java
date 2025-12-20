package com.beaconfire.ordermanagement.dto;

import com.beaconfire.ordermanagement.entity.RefundType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author luluxue
 * @date 2025-12-09
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefundCompletedEvent {
	private String orderId;
	private String refundTransactionId;
	private BigDecimal refundAmount;
	private LocalDateTime refundedAt;
	private RefundType refundType;
	private List<String> returnedItemIds;
}

