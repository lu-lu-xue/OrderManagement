package com.beaconfire.ordermanagement.configuration;

import com.beaconfire.ordermanagement.dto.OrderRefundRequestedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author luluxue
 * @date 2025-12-02
 */
@Component
public class PaymentProducer {
	private final KafkaTemplate<String, OrderRefundRequestedEvent> kafkaTemplate;
	private static final String CANCELLED_TOPIC = "payment.order-refund";
	
	public PaymentProducer(KafkaTemplate<String, OrderRefundRequestedEvent> kafkaTemplate){
		this.kafkaTemplate = kafkaTemplate;
	}
	
	public void sendPaymentRefundEvent(OrderRefundRequestedEvent refundEvent){
		kafkaTemplate.send(CANCELLED_TOPIC, refundEvent.getOrderId(),refundEvent);
	}
}
