package com.yes25.yes255orderpaymentserver.persistance.domain;

import com.yes25.yes255orderpaymentserver.persistance.domain.enumtype.Takeout;
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

    private String orderId;
    private List<Long> productIds;
    private Long userId;
    private BigDecimal orderTotalAmount;
    private Takeout takeout;
    private String addressRaw;
    private String addressDetail;
    private String zipcode;
    private LocalDateTime orderedDate;

    @Builder
    public PreOrder(String orderId, List<Long> productIds, Long userId, BigDecimal orderTotalAmount,
        Takeout takeout, String addressRaw, String addressDetail, String zipcode,
        LocalDateTime orderedDate) {
        this.orderId = orderId;
        this.productIds = productIds;
        this.userId = userId;
        this.orderTotalAmount = orderTotalAmount;
        this.takeout = takeout;
        this.addressRaw = addressRaw;
        this.addressDetail = addressDetail;
        this.zipcode = zipcode;
        this.orderedDate = orderedDate;
    }

    public static PreOrder from(CreateOrderRequest request, String orderId) {
        return PreOrder.builder()
            .orderId(orderId)
            .productIds(request.productIds())
            .userId(request.userId())
            .orderTotalAmount(request.orderTotalAmount())
            .takeout(request.takeout())
            .addressRaw(request.addressRaw())
            .addressDetail(request.addressDetail())
            .zipcode(request.zipcode())
            .orderedDate(LocalDateTime.now())
            .build();
    }

    @Override
    public String toString() {
        return "PreOrder{" +
            "orderId='" + orderId + '\'' +
            ", productIds=" + productIds +
            ", userId=" + userId +
            ", orderTotalAmount=" + orderTotalAmount +
            ", takeout=" + takeout +
            ", addressRaw='" + addressRaw + '\'' +
            ", addressDetail='" + addressDetail + '\'' +
            ", zipcode='" + zipcode + '\'' +
            ", orderedDate=" + orderedDate +
            '}';
    }
}
