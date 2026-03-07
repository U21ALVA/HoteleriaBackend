package com.hoteleria.quantum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class HoteleriaQuantumApplication {

    public static void main(String[] args) {
        SpringApplication.run(HoteleriaQuantumApplication.class, args);
    }
}
