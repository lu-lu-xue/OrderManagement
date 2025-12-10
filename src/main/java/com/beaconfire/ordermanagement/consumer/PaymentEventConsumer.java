package com.beaconfire.ordermanagement.consumer;

import com.beaconfire.ordermanagement.consumer.util.EventProcessorUtil;
import com.beaconfire.ordermanagement.dto.PaymentConfirmedEvent;
import com.beaconfire.ordermanagement.dto.PaymentFailedEvent;
import com.beaconfire.ordermanagement.dto.RefundCompletedEvent;
import com.beaconfire.ordermanagement.entity.OrderStatus;
import com.beaconfire.ordermanagement.service.OrderEventHandler;
import com.beaconfire.ordermanagement.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @author luluxue
 * @date 2025-12-10
 */
@Component
@Slf4j
public class PaymentEventConsumer {
	private final OrderEventHandler orderEventHandler;
	private final EventProcessorUtil eventUtil;
	
	public PaymentEventConsumer(OrderEventHandler orderEventHandler,
	                            EventProcessorUtil eventUtil){
		this.orderEventHandler = orderEventHandler;
		this.eventUtil = eventUtil;
	}
	
	@KafkaListener(topics = "${app.kafka.topics.payment-confirmed}",
			groupId = "order-payment-status")
	public void handlePaymentConfirmed(PaymentConfirmedEvent event){
		eventUtil.processEvent("payment-confirmed",
				event.getOrderId(),
				() -> orderEventHandler.handlePaymentConfirmed(event));
	}
	
	@KafkaListener(
			topics = "${app.kafka.topics.payment-failed}",
			groupId = "order-payment-status"
	)
	public void handlePaymentFailed(PaymentFailedEvent event){
		log.info("Received Payment Failed event for Order: {}", event.getOrderId());
		orderEventHandler.updateOrderStatus(event.getOrderId(), OrderStatus.PAYMENT_FAILED);
	}
	
	@KafkaListener(
			topics = "${app.kafka.topics.payment-refund-done}",
			groupId = "order-refund-status"
	)
	public void handleRefundCompleted(RefundCompletedEvent event){
		log.info("Received Payment Refund Completed event for Order: {}", event.getOrderId());
		orderEventHandler.handleRefundCompletion(event.getOrderId());
	}
}
