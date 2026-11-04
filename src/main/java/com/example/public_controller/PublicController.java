package com.example.public_controller;

import com.example.api_response.ApiResponse;
import com.example.user_info.dto.UserRequestDto;
import com.example.user_info.dto.UserResponseDto;
import com.example.user_info.sevice.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/public")
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

    @PostMapping("/create-newUser")
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(
            @Valid @RequestBody UserRequestDto user) {
        UserResponseDto responseUser = userService.saveUser(user);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", responseUser));
    }
}
