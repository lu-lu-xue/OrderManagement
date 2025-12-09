package com.beaconfire.ordermanagement.configuration;

import com.beaconfire.ordermanagement.dto.InventoryReductionEvent;
import com.beaconfire.ordermanagement.entity.OrderStatus;
import com.beaconfire.ordermanagement.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @author luluxue
 * @date 2025-12-07
 */
@Slf4j
@Component
public class OrderEventListener {
	private final OrderService orderService;
	
	public OrderEventListener(OrderService orderService){
		this.orderService = orderService;
	}
	
	// listening for paymentService
	@KafkaListener(
			topics = "${app.kafka.topics.payment-confirmed}",
			groupId = "order-payment-status"
	)
	public void handlePaymentConfirmed(PaymentConfirmedEvent event){
		log.info("Received Payment Confirmed event for Order: {}", event.getOrderId());
		orderService.updateStatus(event.getOrderId(), orderStatus.CONFIRMED);
	}
	
	@KafkaListener(
			topics = "${app.kafka.topics.payment-refund-done}",
			groupId = "order-refund-status"
	)
	public void handleRefundCompleted(RefundCompletedEvent event){
		log.info("Received Payment Refund Completed event for Order: {}", event.getOrderId());
		orderService.handleRefundCompletion(event.getOrderId());
	}
	
	// listener for when the shipment is picked up/dispatched
	@KafkaListener(
			topics = "${app.kafka.topics.shipment-shipped}",
			groupId = "order-shipment-status"
	)
	public void handleShipmentStarted(ShipmentStartedEvent event){
		log.info("Received Shipment Started event for Order: {}", event.getOrderId());
		
		// update the orderStatus to shipped
		orderService.updateOrderStatus(event.getOrderId(), OrderStatus.SHIPPED);
		
		// later if a tracking number is added to Order entity
		// update the tracking number
	}
	
	@KafkaListener(
			topics = "${app.kafka.topics.shipment-delivered}",
			groupId = "order-shipment-status"
	)
	public void handleShipmentDelivered(ShipmentDeliveredEvent event){
		log.info("Received Shipment Delivered event for Order: {}", event.getOrderId());
		
		// update the orderStatus to shipped
		orderService.updateOrderStatus(event.getOrderId(), OrderStatus.DELIVERED);
	}
	
	// listener for productService (inventory)
	@KafkaListener(
			topics = "${app.kafka.topics.inventory-reserved}",
			groupId = "order-inventory-status"
	)
	public void handleInventoryReserved(InventoryReservedEvent event){
		log.info("Received Inventory Reserved event for Order: {}", event.getOrderId());
		
		// update the ???? orderStatus to be pending???
		orderService.updateOrderStatus(event.getOrderId(), OrderStatus.PROCESSING);
	}
}
