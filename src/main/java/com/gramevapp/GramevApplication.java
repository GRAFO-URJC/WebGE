package com.gramevapp;

import com.gramevapp.web.service.CallablesSubmiter;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com"})
public class GramevApplication {
    private static ConfigurableApplicationContext app;

    public static void main(String[] args) {
        SpringApplication.run(GramevApplication.class, args);
    }

    public static void start() {
        start(new String[] {});
    }

    private static void start(String[] args) {
        if(app == null) {
            app = SpringApplication.run(GramevApplication.class, args);
        }
    }

    public static void stop() {
        if(app != null) {
            app.close();
        }
    }
}
