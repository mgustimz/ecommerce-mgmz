package com.example.ecommercemgmz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EcommerceMgmzApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceMgmzApplication.class, args);
    }

}
