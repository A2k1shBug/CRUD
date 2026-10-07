package com.example.user_info.controller;

import com.example.user_info.dto.UserRequestDto;
import com.example.user_info.model.User;
import com.example.user_info.request_dto.UserResponseDto;
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
    public ResponseEntity<List<User>> getAllUser() {
        List<User> user = userService.getUser();
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid  @RequestBody UserRequestDto user) {
        UserResponseDto responseUser = userService.saveUser(user);
        return new ResponseEntity<>(responseUser, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.dropUser(id);
        String message = "User deleted Successfully";
        return new ResponseEntity<>(message, HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id,
                                           @RequestBody UserRequestDto request) {
        UserResponseDto updateResponseUser = userService.updateUser(id, request);
        return new ResponseEntity<>(updateResponseUser, HttpStatus.OK);
    }
}
