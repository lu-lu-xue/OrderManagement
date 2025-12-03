package com.beaconfire.ordermanagement.dto;

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
public class ReturnItemDTO {
	private String productId;
	private Integer quantity;
}
