package com.restaurant.manager;

import org.springframework.boot.SpringApplication;

public class TestRestaurantManagerBeApplication {

	public static void main(String[] args) {
		SpringApplication.from(RestaurantManagerBeApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
