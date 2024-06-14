package com.yes25.yes255orderpaymentserver.common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    @Bean
    public Queue preOrderQueue() {
        return new Queue("preOrderQueue");
    }

    @Bean
    public Queue paymentQueue() {
        return new Queue("paymentQueue");
    }

    @Bean
    public DirectExchange preOrderExchange() {
        return new DirectExchange("preOrderExchange");
    }

    @Bean
    public DirectExchange paymentExchange() {
        return new DirectExchange("paymentExchange");
    }

    @Bean
    public Binding preOrderBinding(Queue preOrderQueue, DirectExchange preOrderExchange) {
        return BindingBuilder
            .bind(preOrderQueue)
            .to(preOrderExchange)
            .with("preOrderRoutingKey");
    }

    @Bean
    public Binding paymentBinding(Queue paymentQueue, DirectExchange paymentExchange) {
        return BindingBuilder
            .bind(paymentQueue)
            .to(paymentExchange)
            .with("paymentRoutingKey");
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());

        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
