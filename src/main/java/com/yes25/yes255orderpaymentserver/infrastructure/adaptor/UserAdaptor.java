package com.yes25.yes255orderpaymentserver.infrastructure.adaptor;


import com.yes25.yes255orderpaymentserver.application.dto.request.UpdatePointRequest;
import com.yes25.yes255orderpaymentserver.application.dto.request.UpdateUserCartQuantityRequest;
import com.yes25.yes255orderpaymentserver.application.dto.response.UpdatePointResponse;
import com.yes25.yes255orderpaymentserver.common.config.FeignClientConfig;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "userAdaptor", url = "${api.books-users}/users", configuration = FeignClientConfig.class)
public interface UserAdaptor {

    @PatchMapping("/points")
    UpdatePointResponse updatePoint(@RequestBody UpdatePointRequest updatePointRequest);

    @PutMapping("/cart-books/orders")
    void decreaseUserCartQuantity(List<UpdateUserCartQuantityRequest> requests);
}
