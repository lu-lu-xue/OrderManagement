package com.beaconfire.ordermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author luluxue
 * @date 2025-12-01
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryRestockEvent {
	private String orderId;
	private List<ItemToReduce> itemsToRestock;
	private LocalDateTime restockTime;
}
