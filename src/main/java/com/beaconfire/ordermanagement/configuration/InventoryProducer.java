package com.beaconfire.ordermanagement.configuration;

import com.beaconfire.ordermanagement.dto.InventoryReductionEvent;
import com.beaconfire.ordermanagement.dto.InventoryRestockEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author luluxue
 * @date 2025-12-01
 */
@Component
public class InventoryProducer {
	private final KafkaTemplate<String, Object> kafkaTemplate;
	private static final String REDUCTION_TOPIC = "inventory.order-placed";
	private static final String RESTOCK_TOPIC = "inventory.restock-requests";
	
	public InventoryProducer(KafkaTemplate<String, Object> kafkaTemplate){
		this.kafkaTemplate = kafkaTemplate;
	}
	
	public void sendInventoryReductionEvent(InventoryReductionEvent event){
		kafkaTemplate.send(REDUCTION_TOPIC, event.getOrderId(), event);
	}
	
	
	public void sendInventoryRestockEvent(InventoryRestockEvent event){
		kafkaTemplate.send(RESTOCK_TOPIC, event.getOrderId(), event);
	}
}
