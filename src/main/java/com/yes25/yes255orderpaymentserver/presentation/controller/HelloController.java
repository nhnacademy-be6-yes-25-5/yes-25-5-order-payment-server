package com.yes25.yes255orderpaymentserver.presentation.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/hello")
@RestController
@Slf4j
public class HelloController {

    @GetMapping
    public ResponseEntity<String> hello() {
        log.debug("접속 성공");
        return ResponseEntity.ok("접속 성공");
    }
}
