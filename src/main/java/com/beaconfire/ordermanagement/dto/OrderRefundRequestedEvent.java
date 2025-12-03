package com.beaconfire.ordermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author luluxue
 * @date 2025-12-02
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRefundRequestedEvent {
	private String orderId;
	private String paymentTransactionId;
	private BigDecimal refundAmount;
	private String userId;
	private String refundReasonCode;
	private boolean isPartialRefund;
}
