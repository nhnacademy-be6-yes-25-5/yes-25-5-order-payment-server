package com.yes25.yes255orderpaymentserver.infrastructure.adaptor;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "bookAdaptor", url = "http://localhost:8081/books")
public interface BookAdaptor {

    @GetMapping("/check/{bookId}")
    boolean checkStock(@PathVariable Long bookId);
}
