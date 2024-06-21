package com.yes25.yes255orderpaymentserver.infrastructure.adaptor;


import com.yes25.yes255orderpaymentserver.application.dto.request.UpdatePointRequest;
import com.yes25.yes255orderpaymentserver.application.dto.response.UpdatePointResponse;
import com.yes25.yes255orderpaymentserver.common.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "userAdaptor", url = "http://133.186.153.195:8085/users", configuration = FeignClientConfig.class)
public interface UserAdaptor {

    @PatchMapping("{userId}")
    UpdatePointResponse updatePoint(@PathVariable Long userId, @RequestBody UpdatePointRequest updatePointRequest);

}
