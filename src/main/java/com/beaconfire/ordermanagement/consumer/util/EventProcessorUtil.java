package com.beaconfire.ordermanagement.consumer.util;

import com.beaconfire.ordermanagement.exception.OrderNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

/**
 * @author luluxue
 * @date 2025-12-10
 */
@Component
@Slf4j
public class EventProcessorUtil {
	/*
	* generalized event handler, including error handling and logging
	*
	* @param eventType: used for logging
	* @param orderId: used for logging
	* @param handler: business logic
	* */
	public void processEvent(String eventType, String orderId, Runnable handler){
		log.info("Received {} for order: {}", eventType, orderId);
		
		try{
			handler.run();
			log.info("Successfully processed {} for order {}", eventType, orderId);
		} catch (OrderNotFoundException ex){
			// it does not exist, no retry
			log.info("{} - Order not found: {}, message skipped", eventType, orderId);
		} catch (IllegalStateException ex){
			log.warn("{} - Invalid state for order {}: {}, message skipped",
					eventType, orderId, ex.getMessage());
		} catch (DataAccessException ex){
			log.error("{} - Database error for order {}, will retry",
					eventType, orderId, ex);
			// let Kafka retry
			throw ex;
		} catch (Exception ex){
			log.error("{} - Unexpected error for order {}, will retry",
					eventType, orderId, ex);
			throw ex;
		}
	}
}
