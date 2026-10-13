package com.example.health_checker;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class Health {
    @GetMapping
    public String getHealthCondition(HttpServletRequest request){
        return "Ok Fine";
    }
//    @GetMapping("/csrf-token")
//    public CsrfToken getCsrfToken(HttpServletRequest request) {
//        return (CsrfToken) request.getAttribute("_csrf");
//
//
//    }
}
