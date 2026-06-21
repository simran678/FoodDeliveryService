package org.services.fooddeliveryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class FoodDeliveryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FoodDeliveryServiceApplication.class, args);
    }

}
