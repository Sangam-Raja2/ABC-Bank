package com.sangam.abcbank.emiservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.abcbank.emiservice.client")
@EnableDiscoveryClient
@EnableScheduling
public class EmiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmiServiceApplication.class, args);
    }
}
