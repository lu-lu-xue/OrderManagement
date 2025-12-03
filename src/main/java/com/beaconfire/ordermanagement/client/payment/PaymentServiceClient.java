package com.beaconfire.ordermanagement.client.payment;

import com.beaconfire.ordermanagement.dto.PaymentRequestDTO;
import com.beaconfire.ordermanagement.dto.PaymentResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * @author luluxue
 * @date 2025-11-21
 */
@FeignClient(name = "payment-service")
public interface PaymentServiceClient {
	@PostMapping("/api/v1/payment")
	PaymentResponseDTO chargeCustomer(PaymentRequestDTO requestDTto);
	
	@PostMapping("/api/v1/payments/{paymentId}")
	ResponseEntity<PaymentResponseDTO> initiateRefund(
			@PathVariable("paymentId") String paymentId,
			@RequestBody PaymentRequestDTO request);
}