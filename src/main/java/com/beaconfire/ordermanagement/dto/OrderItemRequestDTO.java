package com.beaconfire.ordermanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author luluxue
 * @date 2025-11-19
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemRequestDTO {
	@NotNull(message = "Product ID is required.")
	private String productId;
	
	@Min(value = 1, message = "Quantity must be at least 1.")
	private Integer quantity;
}
