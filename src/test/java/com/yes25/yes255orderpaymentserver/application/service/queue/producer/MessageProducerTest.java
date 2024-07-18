package com.yes25.yes255orderpaymentserver.application.service.queue.producer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.yes25.yes255orderpaymentserver.application.dto.request.StockRequest;
import com.yes25.yes255orderpaymentserver.application.dto.request.UpdateCouponRequest;
import com.yes25.yes255orderpaymentserver.application.dto.request.UpdatePointMessage;
import com.yes25.yes255orderpaymentserver.application.dto.request.UpdateUserCartQuantityRequest;
import com.yes25.yes255orderpaymentserver.application.dto.request.enumtype.OperationType;
import com.yes25.yes255orderpaymentserver.common.jwt.JwtUserDetails;
import com.yes25.yes255orderpaymentserver.persistance.domain.PreOrder;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class MessageProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private MessageProducer messageProducer;

    @Captor
    private ArgumentCaptor<MessagePostProcessor> messagePostProcessorCaptor;

    @Test
    @DisplayName("주문이 확정될 때 메세지를 발행하는지 확인한다.")
    void sendOrderDone() {
        // given
        PreOrder preOrder = mock(PreOrder.class);
        BigDecimal purePrice = new BigDecimal("1000");
        String authToken = "authToken";
        String cartId = "cartId";

        when(preOrder.getPoints()).thenReturn(new BigDecimal("100"));
        when(preOrder.getBookIds()).thenReturn(List.of(1L, 2L));
        when(preOrder.getQuantities()).thenReturn(List.of(1, 2));

        // when
        messageProducer.sendOrderDone(preOrder, purePrice, authToken, cartId);

        // then
        verify(rabbitTemplate).convertAndSend(eq("pointUsedExchange"), eq("pointUsedRoutingKey"), any(UpdatePointMessage.class), messagePostProcessorCaptor.capture());
        verify(rabbitTemplate).convertAndSend(eq("couponUsedExchange"), eq("couponUsedRoutingKey"), anyList(), any(MessagePostProcessor.class));
        verify(rabbitTemplate).convertAndSend(eq("cartDecreaseExchange"), eq("cartDecreaseRoutingKey"), anyList(), any(MessagePostProcessor.class));
    }

    @Test
    @DisplayName("주문 취소시 메세지를 발행하는지 확인한다.")
    void sendOrderCancelMessageByUser() {
        // given
        List<Long> bookIds = List.of(1L, 2L);
        List<Integer> quantities = List.of(1, 2);
        Long couponId = 1L;
        BigDecimal points = new BigDecimal("100");
        BigDecimal purePrice = new BigDecimal("1000");

        JwtUserDetails jwtUserDetails = mock(JwtUserDetails.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        SecurityContextHolder.setContext(securityContext);

        when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwtUserDetails);
        when(jwtUserDetails.accessToken()).thenReturn("accessToken");

        // when
        messageProducer.sendOrderCancelMessageByUser(bookIds, quantities, List.of(couponId), points, purePrice);

        // then
        verify(rabbitTemplate).convertAndSend(eq("couponUnusedExchange"), eq("couponUnusedRoutingKey"), any(UpdateCouponRequest.class), messagePostProcessorCaptor.capture());
        verify(rabbitTemplate).convertAndSend(eq("stockIncreaseExchange"), eq("stockIncreaseRoutingKey"), any(StockRequest.class), any(MessagePostProcessor.class));
        verify(rabbitTemplate).convertAndSend(eq("pointReturnExchange"), eq("pointReturnRoutingKey"), any(UpdatePointMessage.class), any(MessagePostProcessor.class));
    }

    @Test
    @DisplayName("DLX 메세지를 발행하는지 확인한다.")
    void sendDlxMessage() {
        // given
        String dlxExchange = "dlxExchange";
        String routingKey = "routingKey";
        Object message = new Object();

        // when
        messageProducer.sendDlxMessage(dlxExchange, routingKey, message);

        // then
        verify(rabbitTemplate).convertAndSend(dlxExchange, routingKey, message);
    }

    @Test
    @DisplayName("메세지를 발행하는지 확인한다.")
    void sendMessage() {
        // given
        String exchange = "exchange";
        String routingKey = "routingKey";
        Object message = new Object();
        String authToken = "authToken";

        // when
        messageProducer.sendMessage(exchange, routingKey, message, authToken);

        // then
        verify(rabbitTemplate).convertAndSend(eq(exchange), eq(routingKey), eq(message), messagePostProcessorCaptor.capture());
    }
}
