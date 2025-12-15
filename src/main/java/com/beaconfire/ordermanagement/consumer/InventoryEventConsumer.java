package com.beaconfire.ordermanagement.consumer;

import com.beaconfire.ordermanagement.consumer.util.EventProcessorUtil;
import com.beaconfire.ordermanagement.dto.InventoryReductionEvent;
import com.beaconfire.ordermanagement.dto.InventoryReservationFailedEvent;
import com.beaconfire.ordermanagement.dto.InventoryReservedEvent;
import com.beaconfire.ordermanagement.service.OrderEventHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @author luluxue
 * @date 2025-12-10
 */
@Component
public class InventoryEventConsumer {
	private final EventProcessorUtil eventUtil;
	private final OrderEventHandler eventHandler;
	
	public InventoryEventConsumer(OrderEventHandler eventHandler,
	                              EventProcessorUtil eventUtil){
		this.eventHandler = eventHandler;
		this.eventUtil = eventUtil;
	}
	
	@KafkaListener(
			topics = "${app.kafka.topics.inventory-reserved}",
			groupId = "inventory-reserved-status"
	)
	public void handleInventoryReserved(InventoryReservedEvent event){
		eventUtil.processEvent(
				"inventory.reserved-confirmation",
				event.getOrderId(),
				() -> eventHandler.handleInventoryReserved(event)
		);
	}
	
	@KafkaListener(
			topics = "${app.kafka.topics.inventory-reservation-failed}",
			groupId = "inventory-reserved-status"
	)
	public void handleInventoryReservationFailed(InventoryReservationFailedEvent event){
		eventUtil.processEvent(
				"inventory.reserved-confirmation",
				event.getOrderId(),
				() -> eventHandler.handleInventoryReservationFailed(event)
		);
	}
}
