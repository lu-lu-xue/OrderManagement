package com.beaconfire.ordermanagement.controller;

import com.beaconfire.ordermanagement.dto.*;
import com.beaconfire.ordermanagement.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;


/**
 * @author luluxue
 * @date 2025-11-17
 */

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
	private final OrderService orderService;
	
	public OrderController(OrderService orderService){
		this.orderService = orderService;
	}
	
	// 1. place an order
	// POST /api/v1/orders
	@PostMapping
	public ResponseEntity<OrderResponseDTO> createOrder(
			@Valid @RequestBody OrderRequestDTO orderRequestDto,
			@RequestHeader("Idempotency-Key") String idempotencyKey,
			@RequestHeader("X-Payment-Token") String paymentToken
			){
		orderRequestDto.setIdempotencyKey(idempotencyKey);
		orderRequestDto.setPaymentMethodToken(paymentToken);
		// delegating to service layer
		OrderResponseDTO newOrder = orderService.createOrder(orderRequestDto);
		
		// return 201 CREATED
		return new ResponseEntity<>(newOrder, HttpStatus.CREATED);
	}
	
	// 2. get order details
	@GetMapping("/{id}")
	public CompletableFuture<ResponseEntity<OrderDetailsDTO>> getOrderDetails(@PathVariable String id){
		CompletableFuture<OrderDetailsDTO> orderDetailsDto = orderService.getOrderDetails(id);
		
		return orderDetailsDto.thenApply(ResponseEntity::ok);
	}
	
	// 3. get all orders
	// with pagination, size, sorting order
	@GetMapping
	public ResponseEntity<Page<OrderResponseDTO>> getAllOrders(
			// configure the default values for number of pages
			// and size and sorting way
			// page = 0: the first page
			// size will default to 20
			// sort will default to "createdAt" in descending order
			@PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
			Pageable pageable){
		Page<OrderResponseDTO> ordersPage = orderService.getAll(pageable);
		
		return ResponseEntity.ok(ordersPage);
	}
	
	// 4. cancel an order
	@PostMapping("/{id}")
	public ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable String id,
	                                                    @RequestBody CancelOrderRequestDTO requestDto){
		OrderResponseDTO orderRequestDTO = orderService.cancelOrder(id, requestDto);
		
		return ResponseEntity.ok(orderRequestDTO);
	}
	
	// 5. return an order
	// ** or return some items from an order
	@PostMapping("/{id}")
	public ResponseEntity<OrderResponseDTO> returnOrder(@PathVariable String id,
	                                                    @RequestBody ReturnOrderRequestDTO requestDto){
		OrderResponseDTO orderRequestDTO = orderService.returnOrder(id, requestDto);
		
		return ResponseEntity.ok(orderRequestDTO);
	}
	
	// 6. return items of an order?
	// integrated into 5??
	
}
