package com.beaconfire.ordermanagement.configuration;

import com.beaconfire.ordermanagement.service.OrderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @author luluxue
 * @date 2025-12-07
 */
@Component
public class OrderEventListener {
	private final OrderService orderService;
	
	public OrderEventListener(OrderService orderService){
		this.orderService = orderService;
	}
	
	// listening for inventory confirmation
	@KafkaListener(
			topics = "${app.kafka.topics.payment-confirmed}",
			groupId = "order-payment-status"
	)
	
}
