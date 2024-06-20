package com.yes25.yes255orderpaymentserver.infrastructure.adaptor;

import com.yes25.yes255orderpaymentserver.application.dto.request.DecreaseInStockRequest;
import com.yes25.yes255orderpaymentserver.common.config.FeignClientConfig;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "bookAdaptor", url = "http://localhost:8061/books", configuration = FeignClientConfig.class)
public interface BookAdaptor {

    @PatchMapping
    void decreaseStock(List<DecreaseInStockRequest> decreaseInStockRequests);
}
