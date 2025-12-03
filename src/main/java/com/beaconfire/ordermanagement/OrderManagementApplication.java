package com.beaconfire.ordermanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients // to enable scanning @FeignClient annotation
public class OrderManagementApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(OrderManagementApplication.class, args);
	}
	
}
