package com.beaconfire.ordermanagement.repository;

import com.beaconfire.ordermanagement.entity.ReturnedItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author luluxue
 * @date 2025-12-11
 */
@Repository
public interface ReturnedItemRepository extends JpaRepository<ReturnedItem, String> {
	List<ReturnedItem> findByIdIn(List<String> ids);
	
	// fetch returned by order id
	@Query("SELECT r FROM ReturnedItem r WHERE r.orderItem.order.id = :orderId")
	List<ReturnedItem> findByOrderId(@Param("orderId") String orderId);
}
