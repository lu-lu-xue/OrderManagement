package com.beaconfire.ordermanagement.service;

import com.beaconfire.ordermanagement.client.payment.PaymentServiceClient;
import com.beaconfire.ordermanagement.client.product.ProductServiceClient;
import com.beaconfire.ordermanagement.configuration.InventoryProducer;
import com.beaconfire.ordermanagement.configuration.NotificationProducer;
import com.beaconfire.ordermanagement.configuration.PaymentProducer;
import com.beaconfire.ordermanagement.dto.*;
import com.beaconfire.ordermanagement.entity.Order;
import com.beaconfire.ordermanagement.entity.OrderItem;
import com.beaconfire.ordermanagement.entity.OrderStatus;
import com.beaconfire.ordermanagement.exception.*;
import com.beaconfire.ordermanagement.repository.OrderRepository;
import feign.FeignException;
import jakarta.transaction.Transactional;
import org.apache.kafka.common.errors.InvalidRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.lang.IllegalStateException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author luluxue
 * @date 2025-11-17
 */

@Service
public class OrderService {
	private final OrderRepository orderRepo;
	private final ProductServiceClient productServiceClient;
	private final NotificationProducer notificationEventProducer;
	private final InventoryProducer inventoryProducer;
	private final PaymentServiceClient paymentServiceClient;
	private final PaymentProducer paymentProducer;
	
	
	public OrderService(OrderRepository orderRepo,
	                    ProductServiceClient productServiceClient,
	                    NotificationProducer notificationEventProducer,
	                    InventoryProducer inventoryProducer,
	                    PaymentServiceClient paymentServiceClient,
	                    PaymentProducer paymentProducer){
		this.orderRepo = orderRepo;
		this.productServiceClient = productServiceClient;
		this.notificationEventProducer = notificationEventProducer;
		this.inventoryProducer = inventoryProducer;
		this.paymentServiceClient = paymentServiceClient;
		this.paymentProducer = paymentProducer;
	}
	
	// 1. place an order
	@Transactional
	public OrderResponseDTO createOrder(OrderRequestDTO orderRequestDto){
		// !!!! check idempotency
		String clientKey = orderRequestDto.getIdempotencyKey();
		Optional<Order> existing = orderRepo.findByIdempotencyKey(clientKey);
		
		if (existing.isPresent()){
			// unwrap data from Optional
			return OrderMapper.toResponseDTO(existing.get());
		}
		
		// init items and subtotal
		List<OrderItem> items = new ArrayList<>();
		BigDecimal grandTotal = BigDecimal.ZERO;
		
		// 1. fetch prices, calculate subtotals, and build OrderItem
		for (OrderItemRequestDTO itemRequest: orderRequestDto.getItems()){
			String productId = itemRequest.getProductId();
			Integer quantity = itemRequest.getQuantity();
			
			// 1a. check Inventory/availability (synchronous Feign call)
			if (!productServiceClient.isProductAvailable(productId, quantity)) {
				throw new InventoryNotAvailableException("Sorry, the inventory of this product is not available right now.");
			}
			
			// 1b. Fetch details for financial snapshots (synch Feign call
			
			ProductDetailsDTO productDetails;
			try {
				productDetails = productServiceClient.getProductDetails(productId);
			} catch (Exception ex) {
				throw new ProductNotFoundException("Could not retrieve details for this product: " + productId);
			}
			
			// 1c. Financial Calculation
			BigDecimal unitPrice = productDetails.getUnitPrice();
			BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
			
			// 1d. create orderItem entity
			OrderItem orderItem = OrderItem.builder()
					.productId(productId)
					.productName(productDetails.getProductName())  // denormalized data
					.unitPrice(unitPrice)                          // denormalized data
					.quantity(quantity)
					.subtotal(subtotal)
					.build();
			
			items.add(orderItem);
//			grandTotal.add(subtotal);       // function of BigDecimal
			grandTotal = grandTotal.add(subtotal);
		}
		
		// 2. request payment
		// 2.1 validate the payment
		// build paymentRequestDTO
		String preGenereatedOrderId = UUID.randomUUID().toString();
		PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO(
				preGenereatedOrderId,
				orderRequestDto.getUserId(),
				grandTotal,
				orderRequestDto.getPaymentMethodToken()
		);
		
		// synchronous payment call
		try{
			PaymentResponseDTO paymentResponse = paymentServiceClient.chargeCustomer(paymentRequestDTO);
			
			// 1. map business logic failures
			if (paymentResponse.getStatus().equals("FAILED")){
				ErrorResponseDTO error = paymentResponse.getError();
				
				// throw specific local exception based on the payment service's code
				switch(error.getCode()){
					case "CARD_DECLINED":
					case "INSUFFICIENT_FUNDS":
						throw new PaymentFailureException(error.getMessage(), error.getCode());
					case "INVALID_PAYLOAD":
						throw new InvalidRequestException(error.getMessage());
					default:
						throw new PaymentFailureException("Unknown payment error.", error.getCode());
				}
			}
		} catch(FeignException.ServiceUnavailable ex){
			throw new PaymentServiceUnavailableException("Payment service is currently unavailable.", ex);
		}
		
		// if success
		// 2. if valid, create order entity
		Order newOrder = Order.builder()
				.userId(orderRequestDto.getUserId())
				.items(items)
				.totalAmount(grandTotal)
				.status(OrderStatus.PENDING)
				.createdAt(LocalDateTime.now())
				.idempotencyKey(clientKey)
				.build();
		
		// 3. establish bi-directional links and persist???
		// there is @ManyToOne relationship between Order and OrderItem
		for (OrderItem item: items){
			item.setOrder(newOrder);
		}
		
		// save the newOrder
		Order savedOrder = orderRepo.save(newOrder);
		
		// 4. send an event to productService to reduce the inventory????
		// 4.1 map the persisted OrderItems into the event DTO format
		List<ItemToReduce> itemsToReduce = savedOrder.getItems().stream()
				.map(item -> new ItemToReduce(item.getProductId(), item.getQuantity()))
				.toList();
		
		InventoryReductionEvent inventoryEvent = new InventoryReductionEvent(
				savedOrder.getId(),
				itemsToReduce,
				LocalDateTime.now()
		);
		
		// 4.2 publish the inventoryEvent
		inventoryProducer.sendInventoryReductionEvent(inventoryEvent);
		
		// 5. send an event to NotificationService for orderPlaced email
		// 5.1 build the event
		OrderPlacedNotificationEvent notificationEvent = new OrderPlacedNotificationEvent(
				savedOrder.getId(),
				savedOrder.getUserId(),
				savedOrder.getTotalAmount(),
				savedOrder.getCreatedAt(),
				"orderDetailsURL"
		);
		
		// 5.2 publish the notificationEvent
		notificationEventProducer.sendOrderPlacedNotificationEvent(notificationEvent);
		
		return OrderMapper.toResponseDTO(savedOrder);
	}
	
	// 2. get order details
	public OrderResponseDTO getOrderDetails(String orderId){
		// 1. fetch the entity using Optional - prevent NullPointerException
		Order order = orderRepo.findById(orderId)
				.orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));
		
		// 2. convert the entity to a ResponseDTO for external transfer
		return OrderMapper.toResponseDTO(order);
	}
	
	// 3. get all orders (history)
	// with pagination, size, sorting order
	public Page<OrderResponseDTO> getAll(Pageable pageable){
		// 1. repo returns a Page<Order>
		Page<Order> orderPage = orderRepo.findAll(pageable);
		
		// 2. map the contents of the Page<Order> to Page<OrderResponseDTO>
//		List<OrderResponseDTO> ordersDTO = new ArrayList<>();
//		for (Order order: orders){
//			OrderResponseDTO orderDTO = OrderMapper.toResponseDTO(order);
//			ordersDTO.add(orderDTO);
//		}
		
//		return orderPage.map(OrderMapper::toResponseDTO);
		// map contents of Page<Order> to Page<OrderResponseDTO>
		return orderPage.map(order -> OrderMapper.toResponseDTO(order));
	}
	
	// 3. cancel an order
	@Transactional
	public OrderResponseDTO cancelOrder(String orderId, CancelOrderRequestDTO requestDto){
		// 1. fetch the order
		Order order = orderRepo.findById(orderId)
				.orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));
		
		// 2. apply business rule: can only cancel if the status is not shipped or delivered
		validateCancellationEligibility(order);
		
		// 3. update status (the core action)
		order.setStatus(OrderStatus.CANCELLED);
		
		// 4. save and return DTO
		Order savedOrder = orderRepo.save(order);
		
		// 5. publish an event using Kafka (KafkaTemplate) to
		// update the product's inventory
		publishInventoryRestockEvent(savedOrder);
		
		// 6. publish an event to paymentService
		publishRefundEvent(savedOrder, requestDto.getCancelReasonCode(), false);
		
		return OrderMapper.toResponseDTO(savedOrder);
	}
	
	// 4. return an order
	@Transactional
	public OrderResponseDTO returnOrder(String orderId, ReturnOrderRequestDTO requestDto){
		// 1. fetch the order
		Order order = orderRepo.findById(orderId)
				.orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));
		
		// 2. apply business rule: can only cancel if the status is Delivered
		validateRreturnEligibility(order);
		
		// 3. update status
		order.setStatus(OrderStatus.RETURNED);
		
		// 4. save and return DTO
		Order savedOrder = orderRepo.save(order);
		
		// 5. publish an event to
		// update the inventory through ProductService
		publishInventoryRestockEvent(savedOrder);
		
		// 6. publish an event to paymentService
		publishRefundEvent(savedOrder, requestDto.getReturnReasonCode(), true);
		
		return OrderMapper.toResponseDTO(savedOrder);
	}
	
	// code cleaning
	private void validateCancellationEligibility(Order order){
		OrderStatus status = order.getStatus();
		if(status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED){
			throw new IllegalStateException("Order ID " + order.getId() + "cannot be cancelled as it has already shipped.");
		}
		
		if (status == OrderStatus.CANCELLED){
			throw new IllegalStateException("Order ID " + order.getId() + "cannot be cancelled as it has already cancelled.");
		}
	}
	
	private void publishInventoryRestockEvent(Order order){
		// 1 build the inventoryRestockEvent
		List<ItemToReduce> itemsToRestock = order.getItems().stream()
				.map(item -> new ItemToReduce(item.getProductId(), item.getQuantity()))
				.toList();
		
		InventoryRestockEvent inventoryRestockEvent = new InventoryRestockEvent(
				order.getId(),
				itemsToRestock,
				LocalDateTime.now()
		);
		// 2. publish the event
		inventoryProducer.sendInventoryRestockEvent(inventoryRestockEvent);
	}
	
	private void publishRefundEvent(Order order, String reasonCode, boolean ifFullRefund){
		// 1 build the orderRefundRequestEvent
		OrderRefundRequestedEvent orderRefundRequestedEvent = new OrderRefundRequestedEvent(
				order.getId(),
				order.getPaymentTransactionId(),
				order.getTotalAmount(),
				order.getUserId(),
				reasonCode,
				ifFullRefund       // it's a full order cancellation
		);
		
		// 2 publish the refund event
		paymentProducer.sendPaymentRefundEvent(orderRefundRequestedEvent);
	}
	
	
	private void validateRreturnEligibility(Order order){
		OrderStatus status = order.getStatus();
		if(status != OrderStatus.DELIVERED){
			throw new IllegalStateException("Order ID " + order.getId() + "cannot be returned as it has not been delivered.");
		}
	}
}
