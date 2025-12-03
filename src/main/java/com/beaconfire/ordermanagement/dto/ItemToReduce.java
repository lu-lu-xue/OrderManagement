package com.beaconfire.ordermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author luluxue
 * @date 2025-11-30
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemToReduce {
	// DTO for Kafka event
	private String productId;
	private int quantity;
}
