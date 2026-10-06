package com.example.user_info.sevice;

import com.example.user_info.exception.UserNotExist;
import com.example.user_info.exception.UserNotSaveException;
import com.example.user_info.model.User;
import com.example.user_info.repo.UserRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepo;

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public List<User> getUser() {
        return userRepo.findAll();
    }

    public User saveUser(User user) {
        try {
            user.setDateTime(LocalDateTime.now());
            user = userRepo.save(user);
            return user;
        } catch (DataAccessException e) {
            throw new UserNotSaveException("Fail to save user ", e);
        }
    }

    public void dropUser(Long id) {
        try {
            userRepo.deleteById(id);
        } catch (Exception e) {
            throw new UserNotExist("User is not exist in Database", e);
        }
    }

    public User updateUser(Long id, User user) {
        User userExist = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id " + id));

        userExist.setName(user.getName());
        userExist.setEmail(user.getEmail());
        userExist.setDateTime(LocalDateTime.now());
        userExist.setPassword(user.getPassword());
        return userRepo.save(userExist);
    }
}
