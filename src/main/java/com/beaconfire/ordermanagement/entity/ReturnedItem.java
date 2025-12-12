package com.beaconfire.ordermanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author luluxue
 * @date 2025-12-03
 */
@Data
@Entity
@Table(name="returned_items")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReturnedItem {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_item_id", nullable = false)
	private OrderItem orderItem;
	
	@Column(nullable = false)
	private Integer quantity;
	
	@Column(name = "return_reason")
	private String returnReason;
	
	@Column(name = "returned_at")
	private LocalDateTime returnedAt;
	
	@Column(name = "refund_transaction_id", nullable = false)
	private String refundTransactionId;
	
	@Column(name = "refunded_at")
	private LocalDateTime refundedAt;
	
	// access Order through OrderItem
	public Order getOrder(){
		return orderItem.getOrder();
	}
}
