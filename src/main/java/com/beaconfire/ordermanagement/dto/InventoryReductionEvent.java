package com.beaconfire.ordermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author luluxue
 * @date 2025-11-30
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryReductionEvent {
	// for Kafka event
	private String orderId;
	private List<ItemToReduce> itemsToReduce;
	private LocalDateTime reducedTime;
}
