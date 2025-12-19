package com.beaconfire.ordermanagement.service;

import com.beaconfire.ordermanagement.dto.*;
import com.beaconfire.ordermanagement.entity.Order;
import com.beaconfire.ordermanagement.entity.OrderStatus;
import com.beaconfire.ordermanagement.entity.RefundType;
import com.beaconfire.ordermanagement.entity.ReturnedItem;
import com.beaconfire.ordermanagement.exception.OrderNotFoundException;
import com.beaconfire.ordermanagement.repository.OrderRepository;
import com.beaconfire.ordermanagement.repository.ReturnedItemRepository;
import com.beaconfire.ordermanagement.service.publisher.InventoryEventPublisher;
import com.beaconfire.ordermanagement.service.publisher.NotificationEventPublisher;
import com.beaconfire.ordermanagement.service.publisher.PaymentEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author luluxue
 * @date 2025-12-10
 */
@Service
@Slf4j
@Transactional
public class OrderEventHandler {
	private final OrderRepository orderRepo;
	private final ReturnedItemRepository returnedItemRepo;
	private final InventoryEventPublisher inventoryEventPublisher;
	private final NotificationEventPublisher notificationEventPublisher;
	private final PaymentEventPublisher paymentEventPublisher;
	private final OrderService orderService;
	
	public OrderEventHandler(OrderRepository orderRepo,
	                         ReturnedItemRepository returnedRepo,
	                         InventoryEventPublisher inventoryEventPublisher,
	                         NotificationEventPublisher notificationEventPublisher,
	                         PaymentEventPublisher paymentEventPublisher,
	                         OrderService orderService){
		this.orderRepo = orderRepo;
		this.returnedItemRepo = returnedRepo;
		this.inventoryEventPublisher = inventoryEventPublisher;
		this.notificationEventPublisher = notificationEventPublisher;
		this.paymentEventPublisher = paymentEventPublisher;
		this.orderService = orderService;
	}
	
	public void handlePaymentConfirmed(PaymentConfirmedEvent event){
		log.info("Handling payment confirmation for order: {}",
				event.getOrderId());
		
		// 1. fetch order
		Order order = orderRepo.findById(event.getOrderId())
				.orElseThrow(() -> new OrderNotFoundException(
						"Order not found with ID: " + event.getOrderId()
				));
		
		// 2. idempotency check
		if (order.getStatus() == OrderStatus.PAYMENT_CONFIRMED ||
				order.getStatus() == OrderStatus.CONFIRMED ||
		order.getPaymentTransactionId() != null && order.getPaymentTransactionId().equals(event.getPaymentTransactionId())){
			log.info("Order {} already confirmed, skipping", event.getOrderId());
			return;
		}
		
		// 3. check the current status
		if (order.getStatus() != OrderStatus.PENDING){
			throw new IllegalStateException(
					"Cannot confirm payment for order in status: " + order.getStatus()
			);
		}
		
		// 4. now update the order status and payment
		order.setStatus(OrderStatus.PAYMENT_CONFIRMED);
		order.setPaymentTransactionId(event.getPaymentTransactionId());
		order.setPaymentConfirmedAt(event.getConfirmedAt());
		
		Order savedOrder = orderRepo.save(order);
		log.info("Order {} status updated to PAYMENT_CONFIRMED", event.getOrderId());
		
		// 5. publish event to ProductService for inventoryReduction
		inventoryEventPublisher.publishInventoryReductionEvent(savedOrder);
		
		log.info("Inventory reduction event sent for order: {}", savedOrder.getId());
	}
	
	public void handlePaymentFailed(PaymentFailedEvent event){
		log.info("Received Payment Failed event for Order: {}", event.getOrderId());
		
		Order order = orderRepo.findById(event.getOrderId())
				.orElseThrow(() -> new OrderNotFoundException(
						"Order not found with ID: " + event.getOrderId()
				));
		
		// idempotency
		if (order.getStatus() == OrderStatus.PAYMENT_FAILED){
			log.info("Order {} already marked as payment failed", event.getOrderId());
			return;
		}
		
		// update status
		order.setStatus(OrderStatus.PAYMENT_FAILED);
		//order.setPaymentFailureReason(event.getFailedReason());
		
		orderRepo.save(order);
		log.warn("Order {} payment failed: {}", event.getOrderId());
	}
	
	public void handleRefundCompletion(RefundCompletedEvent event) {
		log.info("Received Payment Refund Completed event for Order: {}", event.getOrderId());
		
		// 1. fetch order
		Order order = orderRepo.findById(event.getOrderId())
				.orElseThrow(() -> new OrderNotFoundException(
						"Order not found with ID: " + event.getOrderId()
				));
		
		// 2. check refund type
		switch (event.getRefundType()) {
			case CANCELLATION:
				handleCancellationRefund(order, event);
				break;
			case RETURN:
				handleReturnRefund(order, event);
				break;
			default:
				log.error("Unknown refund type: {}", event.getRefundType());
		}
	}
	
	
	private void handleCancellationRefund(Order order, RefundCompletedEvent event) {
		// 1. idempotency check
		if (order.getStatus() == OrderStatus.CANCELLED) {
			log.warn("Order {} already cancelled", event.getOrderId());
			return;
		}
		
		// 2. update order status
		order.setStatus(OrderStatus.CANCELLED);
//		order.setRefundTransactionId(event.getRefundTransactionId());
//		order.setRefundedAt(event.getRefundedAt());
		
		orderRepo.save(order);
		log.info("Order {} cancelled, refund completed", event.getOrderId());
	}
	
	private void handleReturnRefund(Order order, RefundCompletedEvent event) {
		// check returnedItem list directly
		// 1. check returnedItems
		if (event.getReturnedItemIds() == null ||
				event.getReturnedItemIds().isEmpty()) {
			log.warn("No returned item IDs in refund event for order {}", event.getOrderId());
			return;
		}
		
		// 2. fetch all returned items
		List<ReturnedItem> returnedItems = returnedItemRepo.findByIdIn(event.getReturnedItemIds());
		
		if (returnedItems.isEmpty()) {
			log.error("No returned items found for IDs: {}", event.getReturnedItemIds());
			return;
		}
		
		// 3. update each returned item
		for (ReturnedItem item : returnedItems) {
			// idempotency
			if (item.getRefundTransactionId() != null) {
				log.info("ReturnedItem {} already has refund transaction ID", item.getId());
				continue;
			}
			
			item.setRefundTransactionId(event.getRefundTransactionId());
			item.setRefundedAt(LocalDateTime.now());
		}
		
		returnedItemRepo.saveAll(returnedItems);
		
		// 4. check if it's a full return
		boolean isFullReturn = checkIfFullReturn(order);
		
		// 5. update order status
		if (isFullReturn) {
			if (order.getStatus() != OrderStatus.RETURNED) {
				order.setStatus(OrderStatus.RETURNED);
				log.info("Order {} fully returned", event.getOrderId());
			}
		} else {
			if (order.getStatus() != OrderStatus.PARTIALLY_RETURNED) {
				order.setStatus(OrderStatus.PARTIALLY_RETURNED);
				log.info("Order {} partially returned", event.getOrderId());
			}
		}
		
		// 6. update refund amount for this order
		BigDecimal currentRefund = order.getTotalRefundAmount() != null
				? order.getTotalRefundAmount()
				: BigDecimal.ZERO;
//		order.setRefundAmount(currentRefund.add(event.getRefundAmount()));
		
		orderRepo.save(order);
		log.info("Order {} return refund completed, amount: {}",
				event.getOrderId(), event.getRefundAmount());
	}
	
	/*
	* using stream API, allMatch
	* check each item's remaining quantity
	* */
	private boolean checkIfFullReturn(Order order){
		return order.getItems().stream()
				.allMatch(item -> item.getRemainingQuantity() == 0);
	}
	
	/*
	* handle InventoryReserved InventoryReservedFailed events
	* */
	public void handleInventoryReserved(InventoryReservedEvent event){
		log.info("Handling inventory reserved for order: {}", event.getOrderId());
		
		// 1. fetch order
		Order order = orderRepo.findById(event.getOrderId())
				.orElseThrow(() -> new OrderNotFoundException(
						"Order not found with ID: " + event.getOrderId()
				));
		
		// 2. idempotency
		if (order.getStatus() == OrderStatus.CONFIRMED){
			log.warn("Order {} already confirmed, skipping", event.getOrderId());
			return;
		}
		
		// 3. check current status
		if (order.getStatus() != OrderStatus.PAYMENT_CONFIRMED){
			throw new IllegalStateException(
					"Cannot confirm order in status: " + order.getStatus()
			);
		}
		
		// 4. if payment is successful, confirm the order
		order.setStatus(OrderStatus.CONFIRMED);
		order.setOrderConfirmedAt(LocalDateTime.now());
		
		orderRepo.save(order);
		log.info("Order {} fully confirmed (payment + inventory)", event.getOrderId());
		
		// 5. send notification to user
		notificationEventPublisher.publishOrderConfirmedNotification(order);
		
		
		// 6. publish event to shipmentService to start the shipment
	}
	
	public void handleInventoryReservationFailed(InventoryReservationFailedEvent event){
		log.error("Inventory reduction failed for order: {}", event.getOrderId());
		
		// 1. fetch order
		Order order = orderRepo.findById(event.getOrderId())
				.orElseThrow(() -> new OrderNotFoundException(
						"Order not found with ID: " + event.getOrderId()
				));
		
		// 2. idempotency
		if(order.getStatus() == OrderStatus.INVENTORY_FAILED){
			log.warn("Order {} already marked as INVENTORY_FAILED, skipping refund trigger.", order.getId());
			return;
		}
		
		// 3. update the status
		order.setStatus(OrderStatus.INVENTORY_FAILED);
		// inventoryFailure: order cancellation
		List<ReturnedItem> returnedItems = orderService.createReturnedItems(
				order,
				order.mapAllItemsToRequestDto(),
				"INVENTORY_UNAVAILABLE",
				true
		);
		
		Order savedOrder = orderRepo.save(order);
		orderRepo.flush(); // ensure the ID is re-assigned
		
		// 4. obtain returned ids
		List<String> returnedItemIds = returnedItems.stream()
				.map(ReturnedItem::getId)
						.toList();
		
		// 5. trigger paymentRefund
		paymentEventPublisher.publishPaymentRefundEvent(RefundType.INVENTORY_FAILED, order, order.getTotalAmount(), "Inventory_unavailable", true, returnedItemIds);
	}
	
	// ==== shipment consumer
	public void handleOrderShipped(OrderShippedEvent event){
		log.info("Handling order shipment for order {}", event.getOrderId());
		
		// 1. fetch the order
		Order order = orderRepo.findById(event.getOrderId())
				.orElseThrow(() -> new OrderNotFoundException(
						"Order not found with ID: " + event.getOrderId()
				));
		
		if (order.getStatus() == OrderStatus.SHIPPED){
			log.warn("Order {} has been shipped, skipping", event.getOrderId());
			return;
		}
		
		// update order status
		order.setStatus(OrderStatus.SHIPPED);
		orderRepo.save(order);
		
		// publish event to notificationService
		notificationEventPublisher.publishOrderShippedNotificationEvent(order, event.getShippedAt());
	}
	
	public void handleOrderDelivered(OrderDeliveredEvent event){
		log.info("Handling order delivery for order {}", event.getOrderId());
		
		// 1. fetch the order
		Order order = orderRepo.findById(event.getOrderId())
				.orElseThrow(() -> new OrderNotFoundException(
						"Order not found with ID: " + event.getOrderId()
				));
		
		if (order.getStatus() == OrderStatus.DELIVERED){
			log.warn("Order {} has been delivered, skipping", event.getOrderId());
			return;
		}
		
		// update order status
		order.setStatus(OrderStatus.DELIVERED);
		orderRepo.save(order);
		
		// publish event to notificationService
		notificationEventPublisher.publishOrderDeliveredNotificationEvent(order, event.getDeliveredAt());
	}
}
