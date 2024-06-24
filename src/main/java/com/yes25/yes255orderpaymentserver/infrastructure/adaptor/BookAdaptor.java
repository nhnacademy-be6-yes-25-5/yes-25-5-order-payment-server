package com.yes25.yes255orderpaymentserver.infrastructure.adaptor;

import com.yes25.yes255orderpaymentserver.application.dto.request.StockRequest;
import com.yes25.yes255orderpaymentserver.application.dto.response.ReadBookResponse;
import com.yes25.yes255orderpaymentserver.common.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "bookAdaptor", url = "${api.books-users}/books", configuration = FeignClientConfig.class)
public interface BookAdaptor {

    @PatchMapping
    void updateStock(@RequestBody StockRequest stockRequests);

    @GetMapping("/{bookId}")
    ReadBookResponse findBookById(@PathVariable Long bookId);
}
