package com.yes25.yes255orderpaymentserver.common.config;

import com.yes25.yes255orderpaymentserver.common.jwt.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    .requestMatchers("/swagger/**").permitAll()
                    .requestMatchers("/policies/**").permitAll()
                    .requestMatchers("/orders/none/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/orders").permitAll()
                    .requestMatchers("/payments/**").permitAll()
                    .anyRequest().authenticated());
        return http.build();
    }
}
