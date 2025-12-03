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
 * @date 2025-11-21
 */
//public record ProductDetailsDTO (
//		Long productId,
//		String productName,
//		BigDecimal unitPrice
//){}


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailsDTO{
	@NotNull(message = "product id can't be null.")
	private String productId;
	@NotNull(message = "product name can't be null.")
	private String productName;
	@Positive(message = "price of a product should be positive.")
	private BigDecimal unitPrice;
}