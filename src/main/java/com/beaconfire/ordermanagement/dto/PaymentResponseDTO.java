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
public class PaymentResponseDTO {
	private String paymentTransactionId;
	private String status;              // SUCCESS, FAILED
	private ErrorResponseDTO error;     // included ONLY if status is FAILED
}
