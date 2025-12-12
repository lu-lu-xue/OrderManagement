package com.beaconfire.ordermanagement.service;

import com.beaconfire.ordermanagement.configuration.InventoryProducer;
import com.beaconfire.ordermanagement.dto.*;
import com.beaconfire.ordermanagement.entity.Order;
import com.beaconfire.ordermanagement.entity.OrderStatus;
import com.beaconfire.ordermanagement.entity.ReturnedItem;
import com.beaconfire.ordermanagement.exception.OrderNotFoundException;
import com.beaconfire.ordermanagement.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	private final InventoryProducer inventoryProducer;
	
	public OrderEventHandler(OrderRepository orderRepo,
	                         InventoryProducer inventoryProducer){
		this.orderRepo = orderRepo;
		this.inventoryProducer = inventoryProducer;
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
		// may need to come back to update the paymentConfirmedAt -- add field in Order?? no
		
		Order savedOrder = orderRepo.save(order);
		log.info("Order {} status updated to PAYMENT_CONFIRMED", event.getOrderId());
		
		// 5. publish event to ProductService for inventoryReduction
		List<ItemToReduce> itemsToReduce = savedOrder.getItems().stream()
				.map(item -> new ItemToReduce(item.getProductId(),
						item.getQuantity()))
				.toList();
		
		InventoryReductionEvent inventoryEvent = new InventoryReductionEvent(
				savedOrder.getId(),
				itemsToReduce,
				LocalDateTime.now()
		);
		inventoryProducer.sendInventoryReductionEvent(inventoryEvent);
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
	
	public void handleRefundCompletion(RefundCompletedEvent event){
		log.info("Received Payment Refund Completed event for Order: {}", event.getOrderId());
		
		// 1. fetch order
		Order order = orderRepo.findById(event.getOrderId())
				.orElseThrow(() -> new OrderNotFoundException(
						"Order not found with ID: " + event.getOrderId()
				));
		
		// 2. idempotency check
		if (order.getStatus() == OrderStatus.CANCELLED ||
				order.getStatus() == OrderStatus.RETURNED){
			log.warn("Order {} already in final status {}, skipping refund completion", event.getOrderId(), order.getStatus());
			return;
		}
		
		// 3. update financial audit trail
		List<ReturnedItemId> returnedItems = event.getReturnedItemIds();
		
		if (returnedItems.isEmpty()){
			log.warn("No pending refund items for order {}", event.getOrderId());
			return;
		}
		
		// 4. now update the order status and payment
		order.setStatus(OrderStatus.PAYMENT_CONFIRMED);
		order.setPaymentTransactionId(event.getPaymentTransactionId());
		// may need to come back to update the paymentConfirmedAt -- add field in Order?? no
		
		Order savedOrder = orderRepo.save(order);
		log.info("Order {} status updated to PAYMENT_CONFIRMED", event.getOrderId());
	}
}
