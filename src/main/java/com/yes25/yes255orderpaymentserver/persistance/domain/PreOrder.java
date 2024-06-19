package com.yes25.yes255orderpaymentserver.persistance.domain;

import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.TakeoutType;
import com.yes25.yes255orderpaymentserver.presentation.dto.request.CreateOrderRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PreOrder {

    private String preOrderId;
    private List<Long> bookIds;
    private List<Integer> quantities;
    private List<BigDecimal> prices;
    private Long userId;
    private BigDecimal orderTotalAmount;
    private BigDecimal discountPrice;
    private BigDecimal points;
    private BigDecimal takeoutPrice;
    private BigDecimal shippingFee;
    private TakeoutType takeoutType;
    private String addressRaw;
    private String addressDetail;
    private String zipcode;
    private String reference;
    private LocalDateTime orderedDate;
    private LocalDateTime deliveryDate;
    private String orderUserName;
    private String orderUserEmail;
    private String orderUserPhoneNumber;
    private String receiveName;
    private String receiveEmail;
    private String receivePhoneNumber;
    private Long couponId;

    @Builder
    public PreOrder(String preOrderId, List<Long> bookIds, List<Integer> quantities,
        List<BigDecimal> prices, Long userId, BigDecimal orderTotalAmount,
        BigDecimal discountPrice, TakeoutType takeoutType, String addressRaw, String addressDetail, String zipcode,
        String reference, LocalDateTime orderedDate, LocalDateTime deliveryDate, String orderUserName,
        String orderUserEmail, String orderUserPhoneNumber, String receiveName, String receiveEmail,
        String receivePhoneNumber, Long couponId, BigDecimal points, BigDecimal takeoutPrice,
        BigDecimal shippingFee) {
        this.preOrderId = preOrderId;
        this.bookIds = bookIds;
        this.quantities = quantities;
        this.prices = prices;
        this.userId = userId;
        this.orderTotalAmount = orderTotalAmount;
        this.discountPrice = discountPrice;
        this.takeoutType = takeoutType;
        this.addressRaw = addressRaw;
        this.addressDetail = addressDetail;
        this.zipcode = zipcode;
        this.reference = reference;
        this.orderedDate = orderedDate;
        this.deliveryDate = deliveryDate;
        this.orderUserName = orderUserName;
        this.orderUserEmail = orderUserEmail;
        this.orderUserPhoneNumber = orderUserPhoneNumber;
        this.receiveName = receiveName;
        this.receiveEmail = receiveEmail;
        this.receivePhoneNumber = receivePhoneNumber;
        this.couponId = couponId;
        this.points = points;
        this.takeoutPrice = takeoutPrice;
        this.shippingFee = shippingFee;
    }

    public static PreOrder from(CreateOrderRequest request) {
        return PreOrder.builder()
            .preOrderId(request.orderId())
            .bookIds(request.productIds())
            .quantities(request.quantities())
            .prices(request.prices())
            .userId(request.userId())
            .orderTotalAmount(request.orderTotalAmount())
            .takeoutType(request.takeoutType())
            .addressRaw(request.addressRaw())
            .addressDetail(request.addressDetail())
            .zipcode(request.zipcode())
            .reference(request.reference())
            .orderedDate(LocalDateTime.now())
            .deliveryDate(request.deliveryDate())
            .orderUserName(request.orderName())
            .orderUserEmail(request.orderEmail())
            .orderUserPhoneNumber(request.orderPhoneNumber())
            .receiveName(request.receiveName())
            .receiveEmail(request.receiveEmail())
            .receivePhoneNumber(request.receivePhoneNumber())
            .couponId(request.couponId())
            .points(request.points())
            .discountPrice(request.discountPrice())
            .takeoutPrice(request.takeoutPrice())
            .shippingFee(request.shippingFee())
            .build();
    }

    public Order toEntity(OrderStatus orderStatus, Takeout takeout, BigDecimal purePrice) {
        return Order.builder()
            .orderId(preOrderId)
            .customerId(userId)
            .orderTotalAmount(orderTotalAmount)
            .orderDeliveryAt(orderedDate)
            .orderStatus(orderStatus)
            .takeout(takeout)
            .addressDetail(addressDetail)
            .orderCreatedAt(LocalDateTime.now())
            .addressRaw(addressRaw)
            .zipCode(zipcode)
            .reference(reference)
            .orderUserName(orderUserName)
            .orderUserEmail(orderUserEmail)
            .orderUserPhoneNumber(orderUserPhoneNumber)
            .receiveUserName(receiveName)
            .receiveUserEmail(receiveEmail)
            .receiveUserPhoneNumber(receivePhoneNumber)
            .couponId(couponId)
            .points(points)
            .purePrice(purePrice)
            .build();
    }

    public OrderBook toOrderProduct(Order order, int index) {
        return OrderBook.builder()
            .order(order)
            .bookId(bookIds.get(index))
            .price(prices.get(index))
            .orderProductQuantity(quantities.get(index))
            .build();
    }

    public BigDecimal calculatePurePrice() {
        BigDecimal discount = discountPrice != null ? discountPrice : BigDecimal.ZERO;
        BigDecimal shipping = shippingFee != null ? shippingFee : BigDecimal.ZERO;
        BigDecimal takeout = takeoutPrice != null ? takeoutPrice : BigDecimal.ZERO;
        BigDecimal total = orderTotalAmount != null ? orderTotalAmount : BigDecimal.ZERO;

        return total
            .subtract(discount)
            .subtract(shipping)
            .subtract(takeout);
    }
}
