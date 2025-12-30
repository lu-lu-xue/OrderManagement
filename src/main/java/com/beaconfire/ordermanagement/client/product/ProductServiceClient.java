package com.beaconfire.ordermanagement.client.product;

import com.beaconfire.ordermanagement.dto.ProductDetailsDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author luluxue
 * @date 2025-11-21
 */
@FeignClient(name = "product-service")
@Validated
public interface ProductServiceClient {
	// define a static Logger
	Logger log = LoggerFactory.getLogger(ProductServiceClient.class);
	
	// 1. check inventory
	@GetMapping("/api/v1/products/availability")
	@CircuitBreaker(
			name = "productServiceCB",
			fallbackMethod = "fallbackInventoryCheck"
	)
	boolean isProductAvailable(@RequestParam("productId") String productId, @RequestParam("quantity") Integer quantity);
	
	// fallback logic: what happens when the "Circuit is Open" or Service is down?
	default boolean fallbackInventoryCheck(String id, Integer quantity, Throwable t){
		log.error("Circuit Breaker triggered for Product Service. Reason: {}", t.getMessage());
		
		return false;
	}
	
	
	// 2. check price and name snapshotting
	@GetMapping("/api/v1/products/{id}")
	ProductDetailsDTO getProductDetails(@PathVariable("id") String id);
}
