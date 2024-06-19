package com.yes25.yes255orderpaymentserver.infrastructure.adaptor;

import com.yes25.yes255orderpaymentserver.common.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "bookAdaptor", url = "http://localhost:8081/books", configuration = FeignClientConfig.class)
public interface BookAdaptor {

    @PatchMapping("/{bookId}")
    void decreaseStock(@PathVariable Long bookId, @RequestParam Integer quantity);
}
