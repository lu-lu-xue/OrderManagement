package com.beaconfire.ordermanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author luluxue
 * @date 2025-12-03
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemQuantityDTO {
	@NotNull(message = "Product ID is required.")
	private String productId;
	
	@Min(value = 1, message = "Quantity must be at least 1.")
	private Integer quantity;
}
