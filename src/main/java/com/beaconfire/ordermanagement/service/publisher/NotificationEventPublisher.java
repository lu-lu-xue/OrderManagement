package com.beaconfire.ordermanagement.service.publisher;

import com.beaconfire.ordermanagement.configuration.NotificationProducer;
import com.beaconfire.ordermanagement.dto.*;
import com.beaconfire.ordermanagement.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author luluxue
 * @date 2025-12-17
 */
@Component
@Slf4j
public class NotificationEventPublisher {
	private final NotificationProducer notificationProducer;
	
	public NotificationEventPublisher(NotificationProducer notificationProducer){
		this.notificationProducer = notificationProducer;
	}
	
	public void publishOrderPlacedNotificationEvent(Order order){
		OrderPlacedNotificationEvent notificationEvent = new OrderPlacedNotificationEvent(
				order.getId(),
				order.getUserId(),
				order.getTotalAmount(),
				order.getCreatedAt()
		);
		// 6.2 publish the notificationEvent
		notificationProducer.sendOrderPlacedNotificationEvent(notificationEvent);
	}
	
	public void publishOrderCancelledNotificationEvent(Order order, String cancelReason){
		OrderCancelledNotificationEvent notificationEvent = new OrderCancelledNotificationEvent(
				order.getId(),
				order.getUserId(),
				order.getTotalAmount(),
				cancelReason,
				LocalDateTime.now()
		);
		
		// 7.2 publish the notificationEvent
		notificationProducer.sendOrderCancelledNotificationEvent(notificationEvent);
	}
	
	public void publishOrderReturnedNotificationEvent(Order order, BigDecimal refundTotal, String returnReason, boolean isFullReturn){
		OrderReturnedNotificationEvent notificationEvent = new OrderReturnedNotificationEvent(
				order.getId(),
				order.getUserId(),
				refundTotal,
				returnReason,
				isFullReturn,
				LocalDateTime.now()
		);
		
		// publish the notificationEvent
		notificationProducer.sendOrderReturnedNotificationEvent(notificationEvent);
	}
	
	public void publishOrderConfirmedNotification(Order order){
		OrderConfirmedNotificationEvent confirmEvent = new OrderConfirmedNotificationEvent(
				order.getId(),
				order.getUserId(),
				order.getTotalAmount(),
				order.getOrderConfirmedAt()
		);
		notificationProducer.sendOrderConfirmedNotification(confirmEvent);
	}
	
	public void publishOrderShippedNotificationEvent(Order order, LocalDateTime shippedAt){
		OrderShippedNotificationEvent notificationEvent = new OrderShippedNotificationEvent(
				order.getId(),
				order.getUserId(),
				shippedAt
		);
		notificationProducer.sendOrderShippedNotificationEvent(notificationEvent);
	}
	
	public void publishOrderDeliveredNotificationEvent(Order order, LocalDateTime deliveredAt){
		OrderDeliveredNotificationEvent notificationEvent = new OrderDeliveredNotificationEvent(
				order.getId(),
				order.getUserId(),
				deliveredAt
		);
		notificationProducer.sendOrderDeliveredNotificationEvent(notificationEvent);
	}
}
