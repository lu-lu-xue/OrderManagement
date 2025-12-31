package com.beaconfire.ordermanagement.repository;

import com.beaconfire.ordermanagement.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author luluxue
 * @date 2025-11-17
 */

@Repository
public interface OrderRepository extends JpaRepository <Order, String>{
//	// show order history with pagination
//	Page<Order> findAll(Pageable pageable);
	
	// used for pagination
	// JOIN FETCH: FORCED TO RETRIEVE ALL RELATED ITEMS
	// DISTINCT: SOLVE DUPLICATES
	@Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items")
	Page<Order> findAllWithItems(Pageable pageable);
	
	// used to fetch single order info
	// JOIN FETCH: integrate all related items
	@Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :orderId")
	Optional<Order> findByIdWithItems(@Param("orderId") String orderId);
	
	// findByIdempotencyKey
	Optional<Order> findByIdempotencyKey(String idempotencyKey);
}
