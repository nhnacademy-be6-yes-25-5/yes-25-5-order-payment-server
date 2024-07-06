package com.yes25.yes255orderpaymentserver.common.jwt;

import org.springframework.http.HttpHeaders;

public class HeaderUtils {

    public static HttpHeaders addAuthHeaders(JwtUserDetails jwtUserDetails) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUserDetails.accessToken());
        headers.set("Refresh-Token", jwtUserDetails.refreshToken());
        return headers;
    }
}
