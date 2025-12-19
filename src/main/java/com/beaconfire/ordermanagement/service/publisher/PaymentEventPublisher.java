package com.beaconfire.ordermanagement.service.publisher;

import com.beaconfire.ordermanagement.configuration.PaymentProducer;
import com.beaconfire.ordermanagement.dto.OrderChargeRequestEvent;
import com.beaconfire.ordermanagement.dto.OrderRefundRequestedEvent;
import com.beaconfire.ordermanagement.entity.Order;
import com.beaconfire.ordermanagement.entity.RefundType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author luluxue
 * @date 2025-12-17
 */
@Component
@Slf4j
public class PaymentEventPublisher {
	private final PaymentProducer paymentProducer;
	
	public PaymentEventPublisher(PaymentProducer paymentProducer){
		this.paymentProducer = paymentProducer;
	}
	
	public void publishPaymentRequestEvent(Order order, String paymentMethodToken){
		OrderChargeRequestEvent paymentRequestEvent = new OrderChargeRequestEvent(
				order.getId(),
				order.getUserId(),
				order.getTotalAmount(),
				paymentMethodToken
		);
		paymentProducer.sendPaymentRequestEvent(paymentRequestEvent);
	}
	
	public void publishPaymentRefundEvent(RefundType refundType, Order order,
	                                      BigDecimal refundAmount, String reasonCode,
	                                      boolean isFullRefund, List<String> returnedItemIds){
		log.info("Publishing refund event for Order: {}, Type: {}, Amount: {}",
				order.getId(), refundType, refundAmount);
		
		// 1 build the orderRefundRequestedEvent
		OrderRefundRequestedEvent orderRefundRequestedEvent = new OrderRefundRequestedEvent(
				order.getId(),
				order.getPaymentTransactionId(),
				refundAmount,
				order.getUserId(),
				reasonCode,
				refundType,
				isFullRefund,       // it's a full order cancellation
				returnedItemIds
		);
		
		// 2 publish the refund event based on refundType
		switch(refundType){
			case CANCELLATION:
				paymentProducer.sendPaymentRefundEvent(PaymentProducer.CANCELLED_INVENTORY_FAILED_TOPIC, orderRefundRequestedEvent);
				break;
			case RETURN:
				paymentProducer.sendPaymentRefundEvent(PaymentProducer.RETURNED_TOPIC, orderRefundRequestedEvent);
				break;
			default:
				log.error("Unknown refund type: {}", refundType);
				throw new IllegalArgumentException("Unsupported refund type: " + refundType);
		}
	}
	
}
