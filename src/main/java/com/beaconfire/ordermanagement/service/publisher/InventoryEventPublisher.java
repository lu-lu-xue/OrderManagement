package com.beaconfire.ordermanagement.service.publisher;

import com.beaconfire.ordermanagement.configuration.InventoryProducer;
import com.beaconfire.ordermanagement.dto.InventoryReductionEvent;
import com.beaconfire.ordermanagement.dto.InventoryRestockEvent;
import com.beaconfire.ordermanagement.dto.ItemToReduce;
import com.beaconfire.ordermanagement.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author luluxue
 * @date 2025-12-17
 */
@Component
@Slf4j
public class InventoryEventPublisher {
	private final InventoryProducer inventoryProducer;
	
	public InventoryEventPublisher(InventoryProducer inventoryProducer){
		this.inventoryProducer = inventoryProducer;
	}
	
	public void publishInventoryReductionEvent(Order order){
		// 5.1 map the persisted OrderItems into the event DTO format
		List<ItemToReduce> itemsToReduce = order.getItems().stream()
				.map(item -> new ItemToReduce(item.getProductId(), item.getQuantity()))
				.toList();
		
		InventoryReductionEvent inventoryEvent = new InventoryReductionEvent(
				order.getId(),
				itemsToReduce,
				LocalDateTime.now()
		);
		// 5.2 publish the inventoryEvent
		inventoryProducer.sendInventoryReductionEvent(inventoryEvent);
	}
	
	public void publishInventoryRestockEvent(Order order){
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
}
