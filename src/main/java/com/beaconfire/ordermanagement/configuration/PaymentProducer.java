package com.beaconfire.ordermanagement.configuration;

import com.beaconfire.ordermanagement.dto.OrderChargeRequestEvent;
import com.beaconfire.ordermanagement.dto.OrderRefundRequestedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author luluxue
 * @date 2025-12-02
 */
@Component
public class PaymentProducer {
	private final KafkaTemplate<String, Object> kafkaTemplate;
	private static final String CHARGE_TOPIC = "payment.order-request-charge";
	public static final String CANCELLED_INVENTORY_FAILED_TOPIC = "payment.order-cancelled-refund";
	public static final String RETURNED_TOPIC = "payment.order-returned-refund";
	
	public PaymentProducer(KafkaTemplate<String, Object> kafkaTemplate){
		this.kafkaTemplate = kafkaTemplate;
	}
	
	public void sendPaymentRequestEvent(OrderChargeRequestEvent requestEvent){
		kafkaTemplate.send(CHARGE_TOPIC, requestEvent.getOrderId(), requestEvent);
	}
	
	public void sendPaymentRefundEvent(String topic, OrderRefundRequestedEvent refundEvent){
		kafkaTemplate.send(topic, refundEvent.getOrderId(),refundEvent);
	}
}
