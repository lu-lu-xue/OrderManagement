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
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;
	
	@Column(name = "product_id", nullable = false)
	private String productId;
	
	@Column(name = "product_name")
	private String productName;
	
	@Column(nullable = false)
	private Integer quantity;
	
	@Column(name = "refund_amount", precision = 10, scale = 2)
	private BigDecimal refundAmount;
	
	@Column(name = "return_reason")
	private String returnReason;
	
	@Column(name = "returned_at")
	private LocalDateTime returnedAt;
}
