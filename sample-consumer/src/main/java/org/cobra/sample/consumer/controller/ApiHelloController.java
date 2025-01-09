package org.cobra.sample.consumer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiHelloController {

    @GetMapping(value = "/api/hello")
    public String hello() {
        return "Hello World";
    }
}