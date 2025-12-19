package com.beaconfire.ordermanagement.consumer;

import com.beaconfire.ordermanagement.consumer.util.EventProcessorUtil;
import com.beaconfire.ordermanagement.dto.PaymentConfirmedEvent;
import com.beaconfire.ordermanagement.dto.PaymentFailedEvent;
import com.beaconfire.ordermanagement.dto.RefundCompletedEvent;
import com.beaconfire.ordermanagement.dto.RefundFailedEvent;
import com.beaconfire.ordermanagement.service.OrderEventHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @author luluxue
 * @date 2025-12-10
 */
@Component
public class PaymentEventConsumer {
	private final OrderEventHandler eventHandler;
	private final EventProcessorUtil eventUtil;
	
	public PaymentEventConsumer(OrderEventHandler eventHandler,
	                            EventProcessorUtil eventUtil){
		this.eventHandler = eventHandler;
		this.eventUtil = eventUtil;
	}
	
	@KafkaListener(topics = "${app.kafka.topics.payment-confirmed}",
			groupId = "order-payment-status")
	public void handlePaymentConfirmed(PaymentConfirmedEvent event){
		eventUtil.processEvent("payment-confirmed",
				event.getOrderId(),
				() -> eventHandler.handlePaymentConfirmed(event));
	}
	
	@KafkaListener(
			topics = "${app.kafka.topics.payment-failed}",
			groupId = "order-payment-status"
	)
	public void handlePaymentFailed(PaymentFailedEvent event){
		eventUtil.processEvent("payment-failed",
				event.getOrderId(),
				() -> eventHandler.handlePaymentFailed(event));
	}
	
	@KafkaListener(
			topics = "${app.kafka.topics.payment-refund-done}",
			groupId = "order-refund-status"
	)
	public void handleRefundCompleted(RefundCompletedEvent event){
		eventUtil.processEvent("refund-completed",
				event.getOrderId(),
				() -> eventHandler.handleRefundCompletion(event));
	}
	
	@KafkaListener(
			topics = "${app.kafka.topics.payment-refund-failed}",
			groupId = "order-refund-status"
	)
	public void handleRefundFailed(RefundFailedEvent event){
		eventUtil.processEvent("refund-failed",
				event.getOrderId(),
				() -> eventHandler.handleRefundFailed(event));
	}
}
