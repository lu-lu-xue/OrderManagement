package com.beaconfire.ordermanagement.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * @author luluxue
 * @date 2025-11-19
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestDTO {
	// validation
	@NotNull(message = "Customer ID is required.")
	private String userId;
	
	@NotNull(message = "Customer ID is required.")
	private Long shippingAddressId;
	
	@NotBlank(message = "Payment method cannot be empty.")
	private String paymentMethod;
	
	@NotEmpty(message = "Order must contain at least one item.")
	private List<OrderItemRequestDTO> items;
	
	@NotNull(message = "Idempotency key is required.")
	private String idempotencyKey;
	
	@NotNull(message = "Payment token is required.")
	private String paymentMethodToken;
}