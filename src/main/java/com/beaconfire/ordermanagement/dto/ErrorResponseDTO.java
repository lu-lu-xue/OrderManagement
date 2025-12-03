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
public class ErrorResponseDTO {
	// shared with PaymentService
	private String code;    // CARD_DECLINED, INSUFFICIENT_FUNDS, INVALID_AMOUNT
	private String message; // Detailed message for logging/debugging
	private String type;    // BUSINESS_LOGIC, EXTERNAL_API_ERROR, SYSTEM_ERROR
}
