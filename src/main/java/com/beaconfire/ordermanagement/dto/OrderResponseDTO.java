package com.beaconfire.ordermanagement.dto;

import com.beaconfire.ordermanagement.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author luluxue
 * @date 2025-11-20
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDTO {
	// core Order data
	private String orderId;
	private String userId;
	
	private OrderStatus status;
	
	// financials
	private BigDecimal totalAmount;
	private LocalDateTime createdAt;
	
	// line items (nested response DTO)
	private List<OrderItemResponseDTO> items;
	
	// added the list of returned items
	// it can be empty
	private List<ReturnedItemResponseDTO> returnedItems;
}
