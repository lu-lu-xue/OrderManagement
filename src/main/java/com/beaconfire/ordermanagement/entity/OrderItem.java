package com.beaconfire.ordermanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * @author luluxue
 * @date 2025-11-18
 */

@Data
@Entity
@Table(name="order_items")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItem {
//	@Id
//	@Column(name="order_item_id", updatable=false, nullable=false)
//	private String id = UUID.randomUUID().toString();
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	// foreign key
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable=false)
	private Order order;
	
	// product snapshot
	@Column(name="product_id")
	private String productId;
	
	private String productName;
	private BigDecimal unitPrice;
	private Integer quantity;
	private BigDecimal subtotal;
	private Integer returnedQuantity = 0;
}
