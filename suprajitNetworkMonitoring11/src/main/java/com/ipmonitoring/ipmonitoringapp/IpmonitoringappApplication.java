package com.ipmonitoring.ipmonitoringapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IpmonitoringappApplication {
    public static void main(String[] args) {
        SpringApplication.run(IpmonitoringappApplication.class, args);
    }
}
