package com.beaconfire.ordermanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author luluxue
 * @date 2025-11-17
 */

@Entity
@Table(name="orders")
@Data
@AllArgsConstructor
@Builder
public class Order {
	@Id
	@Column(name="order_id", updatable=false, nullable=false)
	private String id;
	public Order(){
		this.id = UUID.randomUUID().toString();
	}

	// foreign keys
	@Column(name="user_id", updatable=false, nullable=false)
	private String userId;
	
	// lazy fetching is best practice for collections like orderItems
	@OneToMany(mappedBy= "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<OrderItem> items = new ArrayList<>();
	
	@Enumerated(EnumType.STRING)
	private OrderStatus status;  // type is OrderStatus
	
	private BigDecimal totalAmount;
	private LocalDateTime createdAt;
	
	// idempotency key
	@Column(unique = true)
	private String idempotencyKey;
	
	// for cancel or return an order (refund)
	@Column(name = "payment_transaction_id", nullable = true)
	private String paymentTransactionId;
	
	@Column(name = "payment_confirmed_At")
	private LocalDateTime paymentConfirmedAt;
	
	@Column(name = "refund_transaction_id")
	private String refundTransactionId;
	
	@Column(name = "refund_amount")
	private BigDecimal refundAmount;
	
	@Column(name = "refunded_at")
	private LocalDateTime refundedAt;
	
	
	@Column(nullable = true)
	private boolean isFullReturn = false;
	
//	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
//	private List<ReturnedItem> returnedItems = new ArrayList<>();
	
	
	// get all returnedItems
	public List<ReturnedItem> getAllReturnedItems(){
		return items.stream()
				.flatMap(item -> item.getReturnedItems().stream())
				.collect(Collectors.toList());
	}
}
