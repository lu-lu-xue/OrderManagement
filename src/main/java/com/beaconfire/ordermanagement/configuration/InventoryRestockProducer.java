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
public class InventoryRestockProducer {
	private static final String TOPIC = "inventory.restock-requests";
	private final KafkaTemplate<String, InventoryRestockEvent> kafkaTemplate;
	
	public InventoryRestockProducer(KafkaTemplate<String, InventoryRestockEvent> kafkaTemplate){
		this.kafkaTemplate = kafkaTemplate;
	}
	
	public void sendInventoryRestockEvent(InventoryRestockEvent event){
		kafkaTemplate.send(TOPIC, event.getOrderId(), event);
	}
}
