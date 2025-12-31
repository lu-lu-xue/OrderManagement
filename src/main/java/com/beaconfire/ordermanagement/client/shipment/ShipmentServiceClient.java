package com.beaconfire.ordermanagement.client.shipment;

import com.beaconfire.ordermanagement.dto.ShipmentResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author luluxue
 * @date 2025-12-31
 */
@FeignClient(name = "shipment-service")
public interface ShipmentServiceClient {
	// define a static Logger
	Logger log = LoggerFactory.getLogger(ShipmentServiceClient.class);
	
	// 1. check inventory
	@GetMapping("/api/v1/shipment/{orderId}}")
	ShipmentResponseDTO getShipmentInfo(@PathVariable("orderId") String orderId);
}
