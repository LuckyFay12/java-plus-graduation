package ru.practicum.ewm;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@ConfigurationPropertiesScan
public class StatsServer {
    public static void main(String[] args) {
        SpringApplication.run(StatsServer.class, args);
    }

    @Configuration
    public static class WebServerConfig {

        @Bean
        public ServletWebServerFactory servletContainer() {
            TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
            factory.addAdditionalTomcatConnectors(createSecondaryConnector());
            return factory;
        }

        private Connector createSecondaryConnector() {
            Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
            connector.setScheme("http");
            connector.setPort(9090);
            connector.setSecure(false);
            return connector;
        }
    }
}
