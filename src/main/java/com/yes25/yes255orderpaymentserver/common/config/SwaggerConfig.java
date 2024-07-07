package com.yes25.yes255orderpaymentserver.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
            .components(new Components()
                .addSecuritySchemes("bearerAuth",
                    new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"))
                .addParameters("page", new Parameter().in("query").name("page").description("페이지 번호").required(false))
                .addParameters("size", new Parameter().in("query").name("size").description("페이지 크기").required(false))
                .addParameters("sort", new Parameter().in("query").name("sort").description("정렬 기준").required(false))
            )
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .info(new Info()
                .title("orders-payments 서버 API")
                .description("주문-결제 서버 API 명세서 입니다.")
                .version("v0.0.1"))
            .servers(List.of(
                new Server().url("http://localhost:8071").description("로컬 개발 서버"),
                new Server().url("http://133.186.153.195:8070").description("운영 서버")
            ));
    }
}
