package com.beaconfire.ordermanagement.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author luluxue
 * @date 2025-11-20
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponseDTO {
	// includes the snapshot data orderService fetched and saved
	@NotNull
	private String productId;
	@NotNull
	private String productName;
	@Positive
	private Integer quantity;
	@Positive
	private BigDecimal unitPrice;
	private BigDecimal subtotal;
}
