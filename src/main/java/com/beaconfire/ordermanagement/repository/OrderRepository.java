package com.beaconfire.ordermanagement.repository;

import com.beaconfire.ordermanagement.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author luluxue
 * @date 2025-11-17
 */

@Repository
public interface OrderRepository extends JpaRepository <Order, String>{
	// show order history with pagination
	Page<Order> findAll(Pageable pageable);
	
	// findByIdempotencyKey
	Optional<Order> findByIdempotencyKey(String idempotencyKey);
}
