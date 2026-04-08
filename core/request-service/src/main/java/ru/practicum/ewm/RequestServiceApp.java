package ru.practicum.ewm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "ru.practicum.ewm")
@EnableFeignClients(basePackages = "ru.practicum.ewm.client")
public class RequestServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(RequestServiceApp.class, args);
    }
}
