package com.yes25.yes255orderpaymentserver.application.dto.response;

import java.math.BigDecimal;
import java.util.Date;

public record ReadBookResponse(Long bookId, String bookIsbn, String bookName, String bookDescription,
                               String bookAuthor, String bookIndex, String bookPublisher, Date bookPublishDate,
                               BigDecimal bookPrice, BigDecimal bookSellingPrice, String bookImage,
                               Integer bookQuantity, Integer reviewCount, Integer hitsCount, Integer searchCount) {

}
