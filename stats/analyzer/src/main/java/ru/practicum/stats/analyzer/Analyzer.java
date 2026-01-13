package ru.practicum.stats.analyzer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import ru.practicum.stats.analyzer.processor.EventSimilarityProcessor;
import ru.practicum.stats.analyzer.processor.UserActionProcessor;

@SpringBootApplication
@ConfigurationPropertiesScan
@Slf4j
public class Analyzer {
    public static void main(String[] args) {

        ConfigurableApplicationContext context = SpringApplication.run(Analyzer.class, args);

        final UserActionProcessor userActionProcessor = context.getBean(UserActionProcessor.class);
        final EventSimilarityProcessor eventSimilarityProcessor = context.getBean(EventSimilarityProcessor.class);

        Thread userActionsThread = new Thread(userActionProcessor);
        userActionsThread.setName("UserActionHandlerThread");
        userActionsThread.start();

        eventSimilarityProcessor.start();
    }
}