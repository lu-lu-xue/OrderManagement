package com.beaconfire.ordermanagement.exception;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author luluxue
 * @date 2025-11-24
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
	@Builder.Default
	LocalDateTime timestamp = LocalDateTime.now();
	String path;
	int status;
	String error;
	@Builder.Default
	String requestId = UUID.randomUUID().toString();
}
