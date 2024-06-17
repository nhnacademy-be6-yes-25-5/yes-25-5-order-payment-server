package com.yes25.yes255orderpaymentserver.common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue preOrderQueue() {
        return QueueBuilder.durable("preOrderQueue")
            .withArgument("x-dead-letter-exchange", "dlxExchange")
            .withArgument("x-dead-letter-routing-key", "dlx.preOrderQueue")
            .build();
    }

    @Bean
    public Queue paymentQueue() {
        return QueueBuilder.durable("paymentQueue")
            .withArgument("x-dead-letter-exchange", "dlxExchange")
            .withArgument("x-dead-letter-routing-key", "dlx.paymentQueue")
            .build();
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange("dlxExchange");
    }

    @Bean
    public DirectExchange paymentExchange() {
        return new DirectExchange("paymentExchange");
    }

    @Bean
    public DirectExchange preOrderExchange() {
        return new DirectExchange("preOrderExchange");
    }

    @Bean
    public Queue dlqPreOrderQueue() {
        return new Queue("dlq.preOrderQueue");
    }

    @Bean
    public Queue dlqPaymentQueue() {
        return new Queue("dlq.paymentQueue");
    }

    @Bean
    public Binding paymentBinding(Queue paymentQueue, DirectExchange paymentExchange) {
        return BindingBuilder.bind(paymentQueue)
            .to(paymentExchange)
            .with("paymentRoutingKey");
    }

    @Bean
    public Binding preOrderBinding(Queue preOrderQueue, DirectExchange preOrderExchange) {
        return BindingBuilder.bind(preOrderQueue)
            .to(preOrderExchange)
            .with("preOrderRoutingKey");
    }

    @Bean
    public Binding dlqPreOrderBinding(Queue dlqPreOrderQueue, DirectExchange dlxExchange) {
        return BindingBuilder
            .bind(dlqPreOrderQueue)
            .to(dlxExchange)
            .with("dlx.preOrderQueue");
    }

    @Bean
    public Binding dlqPaymentBinding(Queue dlqPaymentQueue, DirectExchange dlxExchange) {
        return BindingBuilder
            .bind(dlqPaymentQueue)
            .to(dlxExchange)
            .with("dlx.paymentQueue");
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
        rabbitTemplate.setChannelTransacted(true);

        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
