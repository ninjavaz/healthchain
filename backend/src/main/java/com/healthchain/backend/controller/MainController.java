package com.healthchain.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/healthchain/api")
public class MainController {
    
    
    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String getHelloMessage() {
        return "Hello";
    }
}
