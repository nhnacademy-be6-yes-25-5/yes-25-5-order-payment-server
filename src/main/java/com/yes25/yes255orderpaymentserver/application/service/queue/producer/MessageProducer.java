package com.yes25.yes255orderpaymentserver.application.service.queue.producer;

import com.yes25.yes255orderpaymentserver.application.dto.request.StockRequest;
import com.yes25.yes255orderpaymentserver.application.dto.request.UpdateCouponRequest;
import com.yes25.yes255orderpaymentserver.application.dto.request.UpdatePointMessage;
import com.yes25.yes255orderpaymentserver.application.dto.request.UpdateUserCartQuantityRequest;
import com.yes25.yes255orderpaymentserver.application.dto.request.enumtype.OperationType;
import com.yes25.yes255orderpaymentserver.common.jwt.JwtUserDetails;
import com.yes25.yes255orderpaymentserver.persistance.domain.PreOrder;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 주문이 확정될 때, 발행되는 메세지입니다.
     *
     * @param preOrder  가주문
     * @param purePrice 취소 금액을 제외한 순수 주문 금액
     * @param authToken 비동기 스레드에서 jwt 사용을 위한 인증 토큰
     * @param cartId 장바구니 ID
     */
    public void sendOrderDone(PreOrder preOrder, BigDecimal purePrice, String authToken,
        String cartId) {
        if (Objects.nonNull(authToken)) {
            UpdatePointMessage updatePointMessage = UpdatePointMessage.of(preOrder.getPoints(),
                purePrice, OperationType.USE);

            List<UpdateCouponRequest> updateCouponRequests = new ArrayList<>();
            for (Long couponId : preOrder.getCouponIds()) {
                UpdateCouponRequest updateCouponRequest = UpdateCouponRequest.from(
                    couponId, OperationType.USE);

                updateCouponRequests.add(updateCouponRequest);
            }

            sendMessage("pointUsedExchange", "pointUsedRoutingKey", updatePointMessage, authToken);
            sendMessage("couponUsedExchange", "couponUsedRoutingKey", updateCouponRequests,
                authToken);

            log.info("쿠폰 사용, 포인트 차감 및 적립 메세지가 발행되었습니다.");
        }

        List<UpdateUserCartQuantityRequest> userCartQuantityRequests = createUserCartQuantityRequests(
            preOrder.getBookIds(), preOrder.getQuantities(), cartId);

        sendMessage("cartDecreaseExchange", "cartDecreaseRoutingKey", userCartQuantityRequests,
            authToken);

        log.info("장바구니 재고 감소 메세지가 발행되었습니다.");
    }

    private List<UpdateUserCartQuantityRequest> createUserCartQuantityRequests(List<Long> bookIds,
        List<Integer> quantities, String cartId) {

        List<UpdateUserCartQuantityRequest> requests = new ArrayList<>();
        for (int i = 0; i < bookIds.size(); i++) {
            UpdateUserCartQuantityRequest request = UpdateUserCartQuantityRequest.of(
                bookIds.get(i), quantities.get(i), cartId);
            requests.add(request);
        }

        return requests;
    }

    /**
     * @param bookIds 주문한 책 ID 리스트
     * @param quantities 주문한 책 수량 리스트
     * @param couponIds 사용한 쿠폰 ID 리스트
     * @param points 주문에 사용한 포인트
     * @param purePrice 취소 금액을 제외한 순수 주문 금액
     * */
    public void sendOrderCancelMessageByUser(List<Long> bookIds,
        List<Integer> quantities, List<Long> couponIds,
        BigDecimal points, BigDecimal purePrice) {
        JwtUserDetails jwtUserDetails = (JwtUserDetails) SecurityContextHolder.getContext()
            .getAuthentication().getPrincipal();
        String authToken =
            jwtUserDetails != null ? "Bearer " + jwtUserDetails.accessToken() : "";

        if (!CollectionUtils.isEmpty(couponIds)) {
            List<UpdateCouponRequest> updateCouponRequests = new ArrayList<>();
            for (Long couponId : couponIds) {
                UpdateCouponRequest updateCouponRequest = UpdateCouponRequest.from(couponId, OperationType.ROLLBACK);
                updateCouponRequests.add(updateCouponRequest);
            }
            sendMessage("couponUnusedExchange", "couponUnusedRoutingKey", updateCouponRequests, authToken);
            log.info("사용자 요청에 의해 결제가 취소 쿠폰 롤백 메세지가 발행되었습니다.");
        }

        StockRequest stockRequest = StockRequest.of(bookIds, quantities, OperationType.INCREASE);
        UpdatePointMessage updatePointMessage = UpdatePointMessage.of(points, purePrice, OperationType.ROLLBACK);

        sendMessage("stockIncreaseExchange", "stockIncreaseRoutingKey", stockRequest, authToken);
        sendMessage("pointReturnExchange", "pointReturnRoutingKey", updatePointMessage, authToken);

        log.info("사용자 요청에 의해 결제가 취소 되어 상품 재고 롤백, 포인트 차감 및 적립 메세지가 발행되었습니다.");
    }

    public <T> void sendDlxMessage(String dlxExchange, String routingKey, T message) {
        rabbitTemplate.convertAndSend(dlxExchange, routingKey, message);
    }

    public <T> void sendMessage(String exchange, String routingKey, T message, String authToken) {
        MessagePostProcessor messagePostProcessor = msg -> {
            msg.getMessageProperties().setHeader("Authorization", authToken);
            return msg;
        };
        rabbitTemplate.convertAndSend(exchange, routingKey, message, messagePostProcessor);
    }

}
