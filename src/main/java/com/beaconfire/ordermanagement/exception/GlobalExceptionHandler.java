package com.beaconfire.ordermanagement.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author luluxue
 * @date 2025-11-17
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(InventoryNotAvailableException.class)
	public ResponseEntity<ErrorResponse> handleInventoryNotAvailable(InventoryNotAvailableException ex,
	                                                                 HttpServletRequest request){
		log.error("InventoryNotAvailableException: {}", ex.getMessage());
		ErrorResponse errorResponse = ErrorResponse.builder()
				.path(request.getRequestURI())
				.status(HttpStatus.CONFLICT.value())
				.error(ex.getMessage())
				.build();
				
		return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
	}
	
	@ExceptionHandler(ProductNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleProductNotFound(ProductNotFoundException ex,
	                                               HttpServletRequest request){
		log.error("ProductNotFoundException: {}", ex.getMessage());
		ErrorResponse errorResponse = ErrorResponse.builder()
				.path(request.getRequestURI())
				.status(HttpStatus.NOT_FOUND.value())
				.error(ex.getMessage())
				.build();
		
		return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(OrderNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex,
	                                                         HttpServletRequest request){
		log.error("OrderNotFoundException: {}", ex.getMessage());
		ErrorResponse errorResponse = ErrorResponse.builder()
				.path(request.getRequestURI())
				.status(HttpStatus.NOT_FOUND.value())
				.error(ex.getMessage())
				.build();
		
		return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex,
	                                                        HttpServletRequest request){
		log.error("IllegalStateException: {}", ex.getMessage());
		ErrorResponse errorResponse = ErrorResponse.builder()
				.path(request.getRequestURI())
				.status(HttpStatus.BAD_REQUEST.value())
				.error(ex.getMessage())
				.build();
		
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}
	
	// payment failure
	@ExceptionHandler(PaymentFailureException.class)
	public ResponseEntity<ErrorResponse> handlePaymentFailure(PaymentFailureException ex,
	                                                          HttpServletRequest request){
		log.error("PaymentFailureException: {}", ex.getMessage());
		ErrorResponse errorResponse = ErrorResponse.builder()
				.path(request.getRequestURI())
				.status(HttpStatus.BAD_REQUEST.value())
				.error(ex.getMessage())
				.build();
		
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(PaymentServiceUnavailableException.class)
	public ResponseEntity<ErrorResponse> handlePaymentServiceUnavailable(PaymentServiceUnavailableException ex,
	                                                                     HttpServletRequest request){
		log.error("PaymentServiceUnavailableException: {}", ex.getMessage());
		ErrorResponse errorResponse = ErrorResponse.builder()
				.path(request.getRequestURI())
				.status(HttpStatus.SERVICE_UNAVAILABLE.value())
				.error(ex.getMessage())
				.build();
		
		return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
	}
	
	@ExceptionHandler(InvalidReturnRequestException.class)
	public ResponseEntity<ErrorResponse> handleIvalidReturnRequest(InvalidReturnRequestException ex,
	                                                               HttpServletRequest request){
		log.error("InvalidReturnRequestException: {}", ex.getMessage());
		ErrorResponse errorResponse = ErrorResponse.builder()
				.path(request.getRequestURI())
				.status(HttpStatus.BAD_REQUEST.value())
				.error(ex.getMessage())
				.build();
		
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}
}
