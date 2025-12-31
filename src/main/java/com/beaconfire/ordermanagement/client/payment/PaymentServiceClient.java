package com.beaconfire.ordermanagement.client.payment;

import com.beaconfire.ordermanagement.client.product.ProductServiceClient;
import com.beaconfire.ordermanagement.dto.PaymentRequestDTO;
import com.beaconfire.ordermanagement.dto.PaymentResponseDTO;
import com.beaconfire.ordermanagement.exception.PaymentServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * @author luluxue
 * @date 2025-11-21
 */
@FeignClient(name = "payment-service")
public interface PaymentServiceClient {
	// define a static Logger
	Logger log = LoggerFactory.getLogger(PaymentServiceClient.class);
	
	// add fault tolerance strategy
	@PostMapping("/api/v1/payment")
	@Retry(name = "paymentRetry")
	@CircuitBreaker(name = "paymentCB", fallbackMethod = "handlePaymentError")
	PaymentResponseDTO chargeCustomer(PaymentRequestDTO requestDTto);
	
	default PaymentResponseDTO handlePaymentError(PaymentRequestDTO request, Throwable t){
		log.error("Payment interface calling failed or CircuitBreaker is triggered. OrderId: {}, Reason: {}",
				request.getOrderId(), t.getMessage());
		
		throw new PaymentServiceUnavailableException("Payment Service is unavailable, " +
				"please check the order status later in the order page", t);
	}
	
	@PostMapping("/api/v1/payments/{paymentId}")
	PaymentResponseDTO initiateRefund(
			@PathVariable("paymentId") String paymentId,
			@RequestBody PaymentRequestDTO request);
	
	@GetMapping("/apiv1/payments/{orderId}")
	PaymentResponseDTO getPaymentByOrder(@PathVariable("orderId") String orderId);
}