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
	public static final String CANCELLED_TOPIC = "payment.order-cancelled-refund";
	public static final String RETURNED_TOPIC = "payment.order-returned-refund";
	
	public PaymentProducer(KafkaTemplate<String, OrderRefundRequestedEvent> kafkaTemplate){
		this.kafkaTemplate = kafkaTemplate;
	}
	
	public void sendPaymentRefundEvent(String topic, OrderRefundRequestedEvent refundEvent){
		kafkaTemplate.send(topic, refundEvent.getOrderId(),refundEvent);
	}
}
