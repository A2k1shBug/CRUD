package com.example.health_checker;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Health {
    @GetMapping
    public String getHealthCondition(){
        return "Ok Fine";
    }
}
