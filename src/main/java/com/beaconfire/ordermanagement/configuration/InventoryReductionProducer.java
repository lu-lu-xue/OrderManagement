package com.beaconfire.ordermanagement.configuration;

import com.beaconfire.ordermanagement.dto.InventoryReductionEvent;
import org.springframework.stereotype.Component;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * @author luluxue
 * @date 2025-11-21
 */
@Component
public class InventoryReductionProducer {
	private static final String TOPIC = "inventory.order-placed";
	private final KafkaTemplate<String, InventoryReductionEvent> kafkaTemplate;
	
	public InventoryReductionProducer(KafkaTemplate<String, InventoryReductionEvent> kafkaTemplate){
		this.kafkaTemplate = kafkaTemplate;
	}
	
	public void sendInventoryReductionEvent(InventoryReductionEvent event){
		kafkaTemplate.send(TOPIC, event.getOrderId(), event);
	}
}
