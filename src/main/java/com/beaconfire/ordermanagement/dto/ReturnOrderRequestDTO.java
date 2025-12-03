package com.beaconfire.ordermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author luluxue
 * @date 2025-12-02
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReturnOrderRequestDTO {
	private String returnReasonCode;    // CUSTOMER_REQUEST, INVENTORY_ISSUE
	private String notes;               // optional
}