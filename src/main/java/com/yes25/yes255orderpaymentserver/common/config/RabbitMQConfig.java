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
    public Queue payQueue() {
        return QueueBuilder.durable("payQueue")
            .build();
    }

    @Bean
    public Queue preOrderCancelQueue() {
        return QueueBuilder.durable("cancelQueue")
            .build();
    }

    @Bean
    public Queue pointUsedQueue() {
        return QueueBuilder.durable("pointUsedQueue")
            .withArgument("x-dead-letter-exchange", "dlxExchange")
            .withArgument("x-dead-letter-routing-key", "dlx.pointUsedQueue")
            .build();
    }

    @Bean
    public Queue couponUsedQueue() {
        return QueueBuilder.durable("couponUsedQueue")
            .withArgument("x-dead-letter-exchange", "dlxExchange")
            .withArgument("x-dead-letter-routing-key", "dlx.couponUsedQueue")
            .build();
    }

    @Bean
    public Queue cartDecreaseQueue() {
        return QueueBuilder.durable("cartDecreaseQueue")
            .withArgument("x-dead-letter-exchange", "dlxExchange")
            .withArgument("x-dead-letter-routing-key", "dlx.cartDecreaseQueue")
            .build();
    }

    @Bean
    public Queue stockDecreaseQueue() {
        return QueueBuilder.durable("stockDecreaseQueue")
            .withArgument("x-dead-letter-exchange", "dlxExchange")
            .withArgument("x-dead-letter-routing-key", "dlx.stockDecreaseQueue")
            .build();
    }

    @Bean
    public Queue stockIncreaseQueue() {
        return QueueBuilder.durable("stockIncreaseQueue")
            .withArgument("x-dead-letter-exchange", "dlxExchange")
            .withArgument("x-dead-letter-routing-key", "dlx.stockIncreaseQueue")
            .build();
    }

    @Bean
    public Queue pointReturnQueue() {
        return QueueBuilder.durable("pointReturnQueue")
            .withArgument("x-dead-letter-exchange", "dlxExchange")
            .withArgument("x-dead-letter-routing-key", "dlx.pointReturnQueue")
            .build();
    }

    @Bean
    public Queue couponUnusedQueue() {
        return QueueBuilder.durable("couponUnusedQueue")
            .withArgument("x-dead-letter-exchange", "dlxExchange")
            .withArgument("x-dead-letter-routing-key", "dlx.couponUnusedQueue")
            .build();
    }

    @Bean
    public Queue dlqStockIncreaseQueue() {
        return new Queue("dlx.stockIncreaseQueue");
    }

    @Bean
    public Queue dlqPointReturnQueue() {
        return new Queue("dlx.pointReturnQueue");
    }

    @Bean
    public Queue dlqCouponUnusedQueue() {
        return new Queue("dlx.couponUnusedQueue");
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange("dlxExchange");
    }

    @Bean
    public DirectExchange couponUsedExchange() {
        return new DirectExchange("couponUsedExchange");
    }

    @Bean
    public DirectExchange cartDecreaseExchange() {
        return new DirectExchange("cartDecreaseExchange");
    }

    @Bean
    public DirectExchange payExchange() {
        return new DirectExchange("payExchange");
    }

    @Bean
    public DirectExchange pointUsedExchange() {
        return new DirectExchange("pointUsedExchange");
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
    public DirectExchange stockDecreaseExchange() {
        return new DirectExchange("stockDecreaseExchange");
    }

    @Bean
    public DirectExchange stockIncreaseExchange() {
        return new DirectExchange("stockIncreaseExchange");
    }

    @Bean
    public DirectExchange pointReturnExchange() {
        return new DirectExchange("pointReturnExchange");
    }

    @Bean
    public DirectExchange couponUnusedExchange() {
        return new DirectExchange("couponUnusedExchange");
    }

    @Bean
    public Queue dlqPreOrderQueue() {
        return new Queue("dlx.preOrderQueue");
    }

    @Bean
    public Queue dlqCouponUsedQueue() {
        return new Queue("dlx.couponUsedQueue");
    }

    @Bean
    public Queue dlqPointUsedQueue() {
        return new Queue("dlx.pointUsedQueue");
    }

    @Bean
    public Queue dlqCartDecreaseQueue() {
        return new Queue("dlx.cartDecreaseQueue");
    }

    @Bean
    public Queue dlqStockDecreaseQueue() {
        return new Queue("dlx.stockDecreaseQueue");
    }

    @Bean
    public Binding stockIncreaseBinding(Queue stockIncreaseQueue, DirectExchange stockIncreaseExchange) {
        return BindingBuilder.bind(stockIncreaseQueue)
            .to(stockIncreaseExchange)
            .with("stockIncreaseRoutingKey");
    }

    @Bean
    public Binding pointReturnBinding(Queue pointReturnQueue, DirectExchange pointReturnExchange) {
        return BindingBuilder.bind(pointReturnQueue)
            .to(pointReturnExchange)
            .with("pointReturnRoutingKey");
    }

    @Bean
    public Binding couponUnusedBinding(Queue couponUnusedQueue, DirectExchange couponUnusedExchange) {
        return BindingBuilder.bind(couponUnusedQueue)
            .to(couponUnusedExchange)
            .with("couponUnusedRoutingKey");
    }

    @Bean
    public Binding cancelBinding(Queue preOrderCancelQueue, DirectExchange cancelExchange) {
        return BindingBuilder.bind(preOrderCancelQueue)
            .to(cancelExchange)
            .with("cancelRoutingKey");
    }

    @Bean
    public Binding cartDecreaseBinding(Queue cartDecreaseQueue, DirectExchange cartDecreaseExchange) {
        return BindingBuilder.bind(cartDecreaseQueue)
            .to(cartDecreaseExchange)
            .with("cartDecreaseRoutingKey");
    }

    @Bean
    public Binding pointUsedBinding(Queue pointUsedQueue, DirectExchange pointUsedExchange) {
        return BindingBuilder.bind(pointUsedQueue)
            .to(pointUsedExchange)
            .with("pointUsedRoutingKey");
    }

    @Bean
    public Binding payBinding(Queue payQueue, DirectExchange payExchange) {
        return BindingBuilder.bind(payQueue)
            .to(payExchange)
            .with("payRoutingKey");
    }

    @Bean
    public Binding preOrderBinding(Queue preOrderQueue, DirectExchange preOrderExchange) {
        return BindingBuilder.bind(preOrderQueue)
            .to(preOrderExchange)
            .with("preOrderRoutingKey");
    }

    @Bean
    public Binding couponUsedBinding(Queue couponUsedQueue, DirectExchange couponUsedExchange) {
        return BindingBuilder.bind(couponUsedQueue)
            .to(couponUsedExchange)
            .with("couponUsedRoutingKey");
    }

    @Bean
    public Binding dlxPreOrderBinding(Queue dlqPreOrderQueue, DirectExchange dlxExchange) {
        return BindingBuilder
            .bind(dlqPreOrderQueue)
            .to(dlxExchange)
            .with("dlx.preOrderQueue");
    }

    @Bean
    public Binding dlxPointUsedBinding(Queue dlqPointUsedQueue, DirectExchange dlxExchange) {
        return BindingBuilder
            .bind(dlqPointUsedQueue)
            .to(dlxExchange)
            .with("dlx.pointUsedQueue");
    }

    @Bean
    public Binding dlxCouponUsedBinding(Queue dlqCouponUsedQueue, DirectExchange dlxExchange) {
        return BindingBuilder
            .bind(dlqCouponUsedQueue)
            .to(dlxExchange)
            .with("dlx.couponUsedQueue");
    }

    @Bean
    public Binding dlxCartDecreaseBinding(Queue dlqCartDecreaseQueue, DirectExchange dlxExchange) {
        return BindingBuilder
            .bind(dlqCartDecreaseQueue)
            .to(dlxExchange)
            .with("dlx.cartDecreaseQueue");
    }

    @Bean
    public Binding stockDecreaseBinding(Queue stockDecreaseQueue, DirectExchange stockDecreaseExchange) {
        return BindingBuilder.bind(stockDecreaseQueue)
            .to(stockDecreaseExchange)
            .with("stockDecreaseRoutingKey");
    }

    @Bean
    public Binding dlxStockDecreaseBinding(Queue dlqStockDecreaseQueue, DirectExchange dlxExchange) {
        return BindingBuilder
            .bind(dlqStockDecreaseQueue)
            .to(dlxExchange)
            .with("dlx.stockDecreaseQueue");
    }

    @Bean
    public Binding dlxStockIncreaseBinding(Queue dlqStockIncreaseQueue, DirectExchange dlxExchange) {
        return BindingBuilder
            .bind(dlqStockIncreaseQueue)
            .to(dlxExchange)
            .with("dlx.stockIncreaseQueue");
    }

    @Bean
    public Binding dlxPointReturnBinding(Queue dlqPointReturnQueue, DirectExchange dlxExchange) {
        return BindingBuilder
            .bind(dlqPointReturnQueue)
            .to(dlxExchange)
            .with("dlx.pointReturnQueue");
    }

    @Bean
    public Binding dlxCouponUnusedBinding(Queue dlqCouponUnusedQueue, DirectExchange dlxExchange) {
        return BindingBuilder
            .bind(dlqCouponUnusedQueue)
            .to(dlxExchange)
            .with("dlx.couponUnusedQueue");
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
