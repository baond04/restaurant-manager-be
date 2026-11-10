package com.restaurant.manager;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class RestaurantManagerBeApplication {

	public static void main(String[] args) {
		// Nhà hàng chạy một múi giờ; mọi LocalDateTime hiểu theo giờ Việt Nam.
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
		SpringApplication.run(RestaurantManagerBeApplication.class, args);
	}

}
