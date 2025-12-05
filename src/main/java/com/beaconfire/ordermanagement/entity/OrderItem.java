package com.beaconfire.ordermanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	// foreign key
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable=false)
	private Order order;
	
	@OneToMany(mappedBy = "orderItem")
	private List<ReturnedItem> returnedItems = new ArrayList<>();
	
	// product snapshot
	@Column(name="product_id")
	private String productId;
	
	private String productName;
	private BigDecimal unitPrice;
	private Integer quantity;
	private BigDecimal subtotal;
	private Integer returnedQuantity = 0;
	
	public Integer getRemainingQuantity(){
		return quantity - returnedQuantity;
	}
	
	// helper method
	public void addReturnedItem(ReturnedItem returnedItem){
		// null check
		if (returnedItem == null){
			throw new IllegalArgumentException("ReturnedItem cannot be null.");
		}
		returnedItems.add(returnedItem);
		returnedItem.setOrderItem(this);        // set both sides of relationship
	}
}
