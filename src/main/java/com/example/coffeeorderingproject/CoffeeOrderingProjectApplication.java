package com.example.coffeeorderingproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CoffeeOrderingProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoffeeOrderingProjectApplication.class, args);
    }

}
