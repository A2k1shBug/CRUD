package com.example.public_controller;

import com.example.user_info.sevice.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublicController {
    private final UserService userService;

    public PublicController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/suggest-username")
    public ResponseEntity<String> suggestUsername(
            @RequestParam String name) {
        String suggestedUsername =
                userService.suggestUsername(name);
        return ResponseEntity.ok(suggestedUsername);
    }
}
