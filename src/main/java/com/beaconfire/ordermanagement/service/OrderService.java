package com.beaconfire.ordermanagement.service;

import com.beaconfire.ordermanagement.client.product.ProductServiceClient;
import com.beaconfire.ordermanagement.configuration.InventoryProducer;
import com.beaconfire.ordermanagement.configuration.NotificationProducer;
import com.beaconfire.ordermanagement.configuration.PaymentProducer;
import com.beaconfire.ordermanagement.dto.*;
import com.beaconfire.ordermanagement.entity.*;
import com.beaconfire.ordermanagement.exception.*;
import com.beaconfire.ordermanagement.repository.OrderRepository;
import com.beaconfire.ordermanagement.service.publisher.InventoryEventPublisher;
import com.beaconfire.ordermanagement.service.publisher.NotificationEventPublisher;
import com.beaconfire.ordermanagement.service.publisher.PaymentEventPublisher;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.lang.IllegalStateException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author luluxue
 * @date 2025-11-17
 */

@Service
@Slf4j
public class OrderService {
	private final OrderRepository orderRepo;
	private final ProductServiceClient productServiceClient;
	private final NotificationProducer notificationEventProducer;
	private final InventoryProducer inventoryProducer;
	private final PaymentProducer paymentProducer;
	private final InventoryEventPublisher inventoryEventPublisher;
	private final NotificationEventPublisher notificationEventPublisher;
	private final PaymentEventPublisher paymentEventPublisher;
	
	
	public OrderService(OrderRepository orderRepo,
	                    ProductServiceClient productServiceClient,
	                    NotificationProducer notificationEventProducer,
	                    InventoryProducer inventoryProducer,
	                    PaymentProducer paymentProducer,
	                    InventoryEventPublisher inventoryEventPublisher,
	                    NotificationEventPublisher notificationEventPublisher,
	                    PaymentEventPublisher paymentEventPublisher){
		this.orderRepo = orderRepo;
		this.productServiceClient = productServiceClient;
		this.notificationEventProducer = notificationEventProducer;
		this.inventoryProducer = inventoryProducer;
		this.paymentProducer = paymentProducer;
		this.inventoryEventPublisher = inventoryEventPublisher;
		this.notificationEventPublisher = notificationEventPublisher;
		this.paymentEventPublisher = paymentEventPublisher;
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
		
		// 1. fetch prices, calculate subtotals, and build OrderItem
		// 1.1 build OrderItem
		List<OrderItem> items = buildOrderItems(orderRequestDto.getItems());
		
		// 1.2 calculate the amount
		BigDecimal grandTotal = calculateGrandTotal(items);
		
		// 2. create order entity
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
		
		// 4. publish event for payment request
		paymentEventPublisher.publishPaymentRequestEvent(savedOrder, orderRequestDto.getPaymentMethodToken());
		
		// 5. send an event to productService to reduce the inventory
		inventoryEventPublisher.publishInventoryReductionEvent(savedOrder);
		
		// 6. send an event to NotificationService for orderPlaced email
		notificationEventPublisher.publishOrderPlacedNotificationEvent(savedOrder);
		
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
		inventoryEventPublisher.publishInventoryRestockEvent(savedOrder);
		
		// 6. publish an event to paymentService
		// 6.1 get the full cancellation
		BigDecimal fullRefundAmount = savedOrder.getTotalAmount();

		paymentEventPublisher.publishPaymentRefundEvent(RefundType.CANCELLATION, savedOrder, fullRefundAmount, requestDto.getCancelReasonCode(), true);
		
		// 7. publish an event to notificationService
		notificationEventPublisher.publishOrderCancelledNotificationEvent(savedOrder, requestDto.getCancelReasonCode());
		
		return OrderMapper.toResponseDTO(savedOrder);
	}
	
	// 4. return an order
	@Transactional
	public OrderResponseDTO returnOrder(String orderId, ReturnOrderRequestDTO requestDto){
		// 1. fetch the order
		Order order = orderRepo.findById(orderId)
				.orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));
		
		// 2. apply business rule:
		// 2.1 can only cancel if the status is Delivered
		validateReturnEligibility(order);
		
		// 2.2. validate items in the order
		boolean isFullReturn = validateReturnItems(order, requestDto);
		
		// 3. update status
		if (!isFullReturn){
			order.setStatus(OrderStatus.PARTIALLY_RETURNED);
		} else {
			order.setFullReturn(true);
			order.setStatus(OrderStatus.RETURNED);
		}
		
		// 3.2 create Order
		createReturnedItems(order, requestDto);
		
		// 4. save and return DTO
		Order savedOrder = orderRepo.save(order);
		
		// 5. publish an event to
		// update the inventory through ProductService
		inventoryEventPublisher.publishInventoryRestockEvent(savedOrder);
		
		// 6. publish an event to paymentService

		// financial calculation for refundTotal
		// it can change in the future if there are
		// strategies like, coupon, promotion, discount
		// .......
		BigDecimal refundTotal = isFullReturn ? order.getTotalAmount() : calculateRefundAmount(order, requestDto.getItemsToReturn());
		
		paymentEventPublisher.publishPaymentRefundEvent(RefundType.RETURN, savedOrder, refundTotal, requestDto.getReturnReasonCode(), isFullReturn);
		
		// 7. publish an event to notificationService
		notificationEventPublisher.publishOrderReturnedNotificationEvent(savedOrder, refundTotal, requestDto.getReturnReasonCode(), isFullReturn);
		
		// 8. publish an event to shipmentService,
		// requesting a tracking number for customer to return??
		return OrderMapper.toResponseDTO(savedOrder);
	}
	
	// code cleaning
	private List<OrderItem> buildOrderItems(List<OrderItemRequestDTO> orderItemDto){
		List<OrderItem> orderItems = new ArrayList<>();
		for (OrderItemRequestDTO itemRequest: orderItemDto){
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
			
			orderItems.add(orderItem);
		}
		
		return orderItems;
	}
	
	private BigDecimal calculateGrandTotal(List<OrderItem> orderItems){
		BigDecimal grandTotal = BigDecimal.ZERO;
		for (OrderItem item: orderItems){
			grandTotal = grandTotal.add(item.getSubtotal()); // function of BigDecimal
		}
		
		return grandTotal;
	}
	
	private void validateCancellationEligibility(Order order){
		OrderStatus status = order.getStatus();
		if(status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED){
			throw new IllegalStateException("Order ID " + order.getId() + "cannot be cancelled as it has already shipped.");
		}
		
		if (status == OrderStatus.CANCELLED){
			throw new IllegalStateException("Order ID " + order.getId() + "cannot be cancelled as it has already cancelled.");
		}
	}
	
	private void createReturnedItems(Order order, ReturnOrderRequestDTO requestDto){
		// create returnedItems
		for (ReturnItemDTO returnItemDto: requestDto.getItemsToReturn()){
			OrderItem orderItem = findOrderItem(order, returnItemDto.getProductId());
			ReturnedItem returnedItem = ReturnedItem.builder()
					.orderItem(orderItem)
					.quantity(returnItemDto.getQuantity())
					.returnReason(requestDto.getReturnReasonCode())
					.returnedAt(LocalDateTime.now())
					.build();
			
			// set up the bi-directional relationship
			orderItem.addReturnedItem(returnedItem);
			
			// update OrderItem returnedQuantity
			orderItem.setReturnedQuantity(
					orderItem.getReturnedQuantity() + returnItemDto.getQuantity()
			);
		}
	}
	
	private void validateReturnEligibility(Order order){
		OrderStatus status = order.getStatus();
		if(status != OrderStatus.DELIVERED){
			throw new IllegalStateException("Order ID " + order.getId() + "cannot be returned as it has not been delivered.");
		}
	}
	
	private boolean validateReturnItems(Order order,
	                                   ReturnOrderRequestDTO requestDto){
		List<ReturnItemDTO> itemsToReturn = requestDto.getItemsToReturn();
		// 1. map for quick lookup of original order items by productId
		Map<String, OrderItem> originalItemsMap = order.getItems().stream()
				.collect(Collectors.toMap(OrderItem::getProductId, Function.identity()));
		
		// init
		int totalOriginalQuantity = 0;
		int totalReturnedQuantity = 0;
		
		for (ReturnItemDTO returnItemDTO: itemsToReturn){
			OrderItem originalItem = originalItemsMap.get(returnItemDTO.getProductId());
			
			// 1. validation: Item must exist in the original order
			if (originalItem == null){
				throw new InvalidReturnRequestException("Product " + returnItemDTO.getProductId() + " was not part of the original order.");
			}
			
			// 2. validation: check if the quantity is valid
			int remaining = originalItem.getRemainingQuantity();
			if (returnItemDTO.getQuantity() > remaining){
				throw new InvalidReturnRequestException("Cannot return " + returnItemDTO.getQuantity() + " units. Only " + originalItem.getQuantity() + " are eligible for return.");
			}
			
			// 3. Determine if this is a full or partial return
			totalOriginalQuantity += originalItem.getQuantity();
			totalReturnedQuantity += returnItemDTO.getQuantity();
			
		}
		// return true if returning entire order
		return totalOriginalQuantity == totalReturnedQuantity;
	}
	
	private OrderItem findOrderItem(Order order, String productId){
		for (OrderItem orderItem: order.getItems()){
			if(orderItem.getProductId().equals(productId)){
				return orderItem;
			}
		}
		
		throw new InvalidReturnRequestException(
				"Product " + productId + " not found in order " + order.getId()
		);
	}
	
	// calculate the full refundAmount for returning items from an order
	// it can contain more details: coupon, promotion, discount, shipment...
	private BigDecimal calculateRefundAmount(Order order, List<ReturnItemDTO> itemsToReturn){
		BigDecimal totalRefund = BigDecimal.ZERO;
		
		// 1. calculate the gross value of the returned items
		for (ReturnItemDTO returnItem: itemsToReturn){
			OrderItem item = findOrderItem(order, returnItem.getProductId());
			
			// calculate the price before applying any other discounts
			BigDecimal grossItemValue = item.getUnitPrice().multiply(BigDecimal.valueOf(returnItem.getQuantity()));
			totalRefund = totalRefund.add(grossItemValue);
		}
		
		return totalRefund;
	}
}
