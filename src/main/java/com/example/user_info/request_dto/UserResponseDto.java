package com.example.user_info.request_dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String password;
    private LocalDateTime createdAt;

}
