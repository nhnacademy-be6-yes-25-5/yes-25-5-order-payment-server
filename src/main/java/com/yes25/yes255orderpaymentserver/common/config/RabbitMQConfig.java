package com.yes25.yes255orderpaymentserver.common.config;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
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
    public Queue preOrderCancelQueue() {
        return QueueBuilder.durable("cancelQueue")
            .build();
    }

    @Bean
    public Queue orderDoneQueue() {
        return QueueBuilder.durable("orderDoneQueue")
            .withArgument("x-dead-letter-exchange", "dlxExchange")
            .withArgument("x-dead-letter-routing-key", "dlx.orderDoneQueue")
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
    public DirectExchange orderDoneExchange() {
        return new DirectExchange("orderDoneExchange");
    }

    @Bean
    public DirectExchange preOrderExchange() {
        return new DirectExchange("preOrderExchange");
    }

    @Bean
    public DirectExchange cancelExchange() {
        return new DirectExchange("cancelExchange");
    }

    @Bean
    public Queue dlqPreOrderQueue() {
        return new Queue("dlx.preOrderQueue");
    }

    @Bean
    public Queue dlqPaymentQueue() {
        return new Queue("dlx.paymentQueue");
    }

    @Bean
    public Queue dlqOrderDoneQueue() {
        return new Queue("dlx.orderDoneQueue");
    }

    @Bean
    public Binding cancelBinding(Queue preOrderCancelQueue, DirectExchange cancelExchange) {
        return BindingBuilder.bind(preOrderCancelQueue)
            .to(cancelExchange)
            .with("cancelRoutingKey");
    }

    @Bean
    public Binding orderDoneBinding(Queue orderDoneQueue, DirectExchange orderDoneExchange) {
        return BindingBuilder.bind(orderDoneQueue)
            .to(orderDoneExchange)
            .with("orderDoneRoutingKey");
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
    public Binding dlxPreOrderBinding(Queue dlqPreOrderQueue, DirectExchange dlxExchange) {
        return BindingBuilder
            .bind(dlqPreOrderQueue)
            .to(dlxExchange)
            .with("dlx.preOrderQueue");
    }

    @Bean
    public Binding dlxPaymentBinding(Queue dlqPaymentQueue, DirectExchange dlxExchange) {
        return BindingBuilder
            .bind(dlqPaymentQueue)
            .to(dlxExchange)
            .with("dlx.paymentQueue");
    }

    @Bean
    public Binding dlxOrderDoneBinding(Queue dlqOrderDoneQueue, DirectExchange dlxExchange) {
        return BindingBuilder
            .bind(dlqOrderDoneQueue)
            .to(dlxExchange)
            .with("dlx.orderDoneQueue");
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
