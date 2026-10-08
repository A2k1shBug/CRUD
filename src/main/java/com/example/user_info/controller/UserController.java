package com.example.user_info.controller;

import com.example.user_info.api_response.ApiResponse;
import com.example.user_info.dto.UserRequestDto;
import com.example.user_info.dto.UserResponseDto;
import com.example.user_info.sevice.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Component
@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/all_user")
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getAllUser() {
        List<UserResponseDto> user = userService.getUser();
        return ResponseEntity.ok(ApiResponse.
                success("Users fetched successfully", user));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(
            @Valid @RequestBody UserRequestDto user) {
        UserResponseDto responseUser = userService.saveUser(user);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", responseUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.dropUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(@PathVariable Long id,
                                                                   @RequestBody UserRequestDto request) {
        UserResponseDto updateResponseUser = userService.updateUser(id, request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("User updated successfully", updateResponseUser));
    }
}
