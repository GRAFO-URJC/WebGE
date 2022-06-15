package com.gramevapp.web.service;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;

import java.util.logging.Logger;


@Configuration
public class MQConfig {
    public static final String RUNS_QUEUE = "message_queue";
    public static final String REPORTS_QUEUE = "reports_queue";
    public static final String EXCHANGE = "EXCHANGE";
    public static final String RUNS_ROUTING_KEY = "message_routingKey";
    public static final String REPORT_ROUTING_KEY = "report_routingKey";

    @Bean
    public Queue queue1() {
        return  new Queue(RUNS_QUEUE);
    }

    @Bean
    public Queue queue2() {
        return  new Queue(REPORTS_QUEUE);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    // Bind with runs queue
    @Bean
    public Binding binding1(Queue queue1, DirectExchange exchange) {
        return BindingBuilder
                .bind(queue1)
                .to(exchange)
                .with(RUNS_ROUTING_KEY);
    }

    // Bind with reports queue
    @Bean
    public Binding binding2(Queue queue2, DirectExchange exchange) {
        return BindingBuilder
                .bind(queue2)
                .to(exchange)
                .with(REPORT_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
      return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate template(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    @Bean
    public SimpleMessageListenerContainer simpleMessageListenerContainer(ConnectionFactory connectionFactory) {
        return new SimpleMessageListenerContainer(connectionFactory);
    }
}
