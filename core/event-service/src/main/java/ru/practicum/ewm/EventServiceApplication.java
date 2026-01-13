package ru.practicum.ewm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.practicum.client.CommentClient;
import ru.practicum.client.RequestClient;
import ru.practicum.client.UserClient;

@SpringBootApplication(scanBasePackages = {
        "ru.practicum.ewm",
        "ru.practicum.client",
        "ru.practicum.exception"
})
@ConfigurationPropertiesScan
@EnableFeignClients(clients = {CommentClient.class, UserClient.class, RequestClient.class})
public class EventServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventServiceApplication.class, args);
    }
}