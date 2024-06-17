package com.yes25.yes255orderpaymentserver.persistance.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Takeout extends Time{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long takeoutId;

    private String takeoutName;
    private String takeoutDescription;
    private BigDecimal takeoutPrice;

    @Builder
    public Takeout(Long takeoutId, String takeoutName, String takeoutDescription,
        BigDecimal takeoutPrice) {
        this.takeoutId = takeoutId;
        this.takeoutName = takeoutName;
        this.takeoutDescription = takeoutDescription;
        this.takeoutPrice = takeoutPrice;
    }
}
