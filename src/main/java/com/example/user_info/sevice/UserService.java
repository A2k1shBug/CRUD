package com.example.user_info.sevice;

import com.example.user_info.dto.UserRequestDto;
import com.example.user_info.exception.UserAlreadyExistsException;
import com.example.user_info.exception.UserNotExist;
import com.example.user_info.exception.UserNotSaveException;
import com.example.user_info.model.User;
import com.example.user_info.repo.UserRepository;
import com.example.user_info.dto.UserResponseDto;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepo;

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public List<UserResponseDto> getUser() {
        return userRepo.findAll()
                .stream()
                .map(user -> toResponse(user))
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponseDto saveUser(UserRequestDto request) {
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhone());
        user.setPassword(request.getPassword());
        user.setDateTime(LocalDateTime.now(ZoneId.of("Asia/Kathmandu")));

        try {
            user=null;
            return toResponse(userRepo.save(user));
        } catch (DataAccessException e) {
            throw new UserNotSaveException("Fail to save user", e);
        }
    }

    private UserResponseDto toResponse(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setUserId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhoneNumber());
        dto.setCreatedAt(user.getDateTime());
        dto.setPassword(user.getPassword());
        return dto;
    }

    public void dropUser(Long id) {
        if (!userRepo.existsById(id)) {
            throw new UserNotExist("User is not exist in Database" + id);
        }
        userRepo.deleteById(id);
    }

    @Transactional
    public UserResponseDto updateUser(Long id, UserRequestDto request) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotExist("User not found with id " + id));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(request.getPassword());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhoneNumber(request.getPhone());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail());
        }

        try {
            return toResponse(userRepo.saveAndFlush(user));
        } catch (DataAccessException e) {
            throw new UserNotSaveException("Fail to update user", e);
        }
    }
}

