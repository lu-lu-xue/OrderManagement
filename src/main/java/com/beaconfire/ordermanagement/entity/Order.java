package com.beaconfire.ordermanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
	private String paymentTransactionId;
}
