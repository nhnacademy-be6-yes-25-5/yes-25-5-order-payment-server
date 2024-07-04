package com.yes25.yes255orderpaymentserver.infrastructure.adaptor;

import com.yes25.yes255orderpaymentserver.application.dto.request.ReadBookInfoResponse;
import com.yes25.yes255orderpaymentserver.application.dto.request.StockRequest;
import com.yes25.yes255orderpaymentserver.application.dto.response.ReadBookResponse;
import com.yes25.yes255orderpaymentserver.common.config.FeignClientConfig;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "bookAdaptor", url = "${api.books-users}/books", configuration = FeignClientConfig.class)
public interface BookAdaptor {

    @PatchMapping
    void updateStock(@RequestBody StockRequest stockRequests);

    @GetMapping("/{bookId}")
    ReadBookResponse findBookById(@PathVariable Long bookId);

    @GetMapping("/orders")
    List<ReadBookInfoResponse> getAllByBookIds(@RequestParam List<Long> bookIdList);
}
