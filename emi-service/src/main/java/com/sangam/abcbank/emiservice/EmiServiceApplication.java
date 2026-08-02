package com.sangam.abcbank.emiservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(
        scanBasePackages = {
                "com.sangam.abcbank.emiservice",
                "com.sangam.abcbank.common"
        }
)
public class EmiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmiServiceApplication.class, args);
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
