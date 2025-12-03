package com.beaconfire.ordermanagement.client.product;

import com.beaconfire.ordermanagement.dto.ProductDetailsDTO;
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
	// 1. check inventory
	@GetMapping("/api/v1/products/availability")
	boolean isProductAvailable(@RequestParam("productId") String productId, @RequestParam("quantity") Integer quantity);
	
	// 2. check price and name snapshotting
	@GetMapping("/api/v1/products/{id}")
	ProductDetailsDTO getProductDetails(@PathVariable("id") String id);
}
