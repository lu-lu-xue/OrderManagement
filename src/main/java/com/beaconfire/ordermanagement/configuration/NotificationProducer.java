package com.beaconfire.ordermanagement.configuration;

import com.beaconfire.ordermanagement.dto.OrderPlacedNotificationEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author luluxue
 * @date 2025-11-30
 */
@Component
public class NotificationProducer {
	private static final String TOPIC = "notification.order-placed";
	private final KafkaTemplate<String, OrderPlacedNotificationEvent> kafkaTemplate;
	
	public NotificationProducer(KafkaTemplate<String, OrderPlacedNotificationEvent> kafkaTemplate){
		this.kafkaTemplate = kafkaTemplate;
	}
	
	public void sendOrderPlacedNotificationEvent(OrderPlacedNotificationEvent event){
		kafkaTemplate.send(TOPIC, event.getUserId(), event);
	}
}
