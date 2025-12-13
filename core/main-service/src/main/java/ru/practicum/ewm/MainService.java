package ru.practicum.ewm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = {
        "ru.practicum.ewm",
        "ru.practicum.client"
})
@ConfigurationPropertiesScan
public class MainService {
    public static void main(String[] args) {
        SpringApplication.run(MainService.class, args);
    }
}