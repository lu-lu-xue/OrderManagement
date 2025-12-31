package com.beaconfire.ordermanagement.service;

import com.beaconfire.ordermanagement.dto.OrderItemResponseDTO;
import com.beaconfire.ordermanagement.dto.OrderResponseDTO;
import com.beaconfire.ordermanagement.dto.ReturnedItemResponseDTO;
import com.beaconfire.ordermanagement.entity.Order;
import com.beaconfire.ordermanagement.entity.OrderItem;
import com.beaconfire.ordermanagement.entity.ReturnedItem;

/**
 * @author luluxue
 * @date 2025-11-25
 */
public class OrderMapper {
	public static OrderResponseDTO toResponseDTO(Order order){
		// build and populate OrderResponseDTO
		return OrderResponseDTO.builder()
				.orderId(order.getId())
				.userId(order.getUserId())
				.status(order.getStatus())
				.totalAmount(order.getTotalAmount())
				.createdAt(order.getCreatedAt())
				.items(order.getItems().stream()  // recursively map items
						.map(OrderMapper::toOrderItemResponseDTO)
						.toList())
				.build();
	}
	
	public static OrderItemResponseDTO toOrderItemResponseDTO(OrderItem item){
		// field-to-field copy for item details
		return OrderItemResponseDTO.builder()
				.productId(item.getProductId())
				.productName(item.getProductName())
				.unitPrice(item.getUnitPrice())
				.quantity(item.getQuantity())
				.subtotal(item.getSubtotal())
				.build();
	}
	
	// add toReturnedItemResponseDTO
	// for getOrderDetails in OrderService
	public static ReturnedItemResponseDTO toReturnedItemResponseDTO(ReturnedItem item){
		return ReturnedItemResponseDTO.builder()
				.id(item.getId())
				.quantity(item.getQuantity())
				.reason(item.getReturnReason())
				.refundAmount(item.getRefundAmount())
				.status(item.getRefundStatus())
				.transactionId(item.getRefundTransactionId())
				.refundedAt(item.getRefundedAt())
				.build();
	}
}
