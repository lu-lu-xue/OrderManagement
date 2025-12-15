package com.beaconfire.ordermanagement.consumer;

import com.beaconfire.ordermanagement.consumer.util.EventProcessorUtil;
import com.beaconfire.ordermanagement.dto.OrderDeliveredEvent;
import com.beaconfire.ordermanagement.dto.OrderShippedEvent;
import com.beaconfire.ordermanagement.service.OrderEventHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @author luluxue
 * @date 2025-12-10
 */
@Component
public class ShipmentEventConsumer {
	private final EventProcessorUtil eventUtil;
	private final OrderEventHandler eventHandler;
	
	public ShipmentEventConsumer(EventProcessorUtil eventUtil,
	                             OrderEventHandler eventHandler){
		this.eventUtil = eventUtil;
		this.eventHandler = eventHandler;
	}
	
	@KafkaListener(
			topics = "${app.kafka.topics.shipment-shipped}",
			groupId = "shipment-status"
	)
	public void handleOrderShipped(OrderShippedEvent event){
		eventUtil.processEvent("shipment.started",
				event.getOrderId(),
				() -> eventHandler.handleOrderShipped(event));
	}
	
	@KafkaListener(
			topics = "${app.kafka.topics.shipment-delivered}",
			groupId = "shipment-status"
	)
	public void handleOrderDelivered(OrderDeliveredEvent event){
		eventUtil.processEvent("shipment.delivered",
				event.getOrderId(),
				() -> eventHandler.handleOrderDelivered(event));
	}
}
