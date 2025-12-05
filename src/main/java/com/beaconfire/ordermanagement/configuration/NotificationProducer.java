package com.beaconfire.ordermanagement.configuration;

import com.beaconfire.ordermanagement.dto.OrderCancelledNotificationEvent;
import com.beaconfire.ordermanagement.dto.OrderPlacedNotificationEvent;
import com.beaconfire.ordermanagement.dto.OrderReturnedNotificationEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author luluxue
 * @date 2025-11-30
 */
@Component
public class NotificationProducer {
	private static final String CREATE_TOPIC = "notification.order-placed";
	private static final String CANCELLED_TOPIC = "notification.order-cancelled";
	private static final String RETURNED_TOPIC = "notification.order-returned";
	private final KafkaTemplate<String, Object> kafkaTemplate;
	
	public NotificationProducer(KafkaTemplate<String, Object> kafkaTemplate){
		this.kafkaTemplate = kafkaTemplate;
	}
	
	public void sendOrderPlacedNotificationEvent(OrderPlacedNotificationEvent event){
		kafkaTemplate.send(CREATE_TOPIC, event.getUserId(), event);
	}
	
	public void sendOrderCancelledNotificationEvent(OrderCancelledNotificationEvent event){
		kafkaTemplate.send(CANCELLED_TOPIC, event.getUserId(), event);
	}
	
	public void sendOrderReturnedNotificationEvent(OrderReturnedNotificationEvent event){
		kafkaTemplate.send(RETURNED_TOPIC, event.getUserId(), event);
	}
}
