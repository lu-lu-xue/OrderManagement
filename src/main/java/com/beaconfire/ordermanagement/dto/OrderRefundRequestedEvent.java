package com.beaconfire.ordermanagement.dto;

import com.beaconfire.ordermanagement.entity.RefundType;
import com.beaconfire.ordermanagement.entity.ReturnedItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

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
	private RefundType refundType;
	private boolean isFullRefund;
	// returnedItems
	private List<String> returnedItemIds;
}
