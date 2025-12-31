package com.beaconfire.ordermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author luluxue
 * @date 2025-12-31
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShipmentResponseDTO {
	private String trackingNumber;
	private String carrier;
	private String status;
	private LocalDateTime estimatedArrival;
}
