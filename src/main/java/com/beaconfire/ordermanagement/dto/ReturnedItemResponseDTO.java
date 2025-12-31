package com.beaconfire.ordermanagement.dto;

import com.beaconfire.ordermanagement.entity.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author luluxue
 * @date 2025-12-30
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReturnedItemResponseDTO {
	private String id;
	private Integer quantity;
	private String reason;
	private BigDecimal refundAmount;
	private RefundStatus status;
	private String transactionId;
	private LocalDateTime refundedAt;
}
