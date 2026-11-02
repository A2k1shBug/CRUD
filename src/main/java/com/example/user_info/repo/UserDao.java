package com.example.user_info.repo;

import com.example.user_info.model.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDao {
    List<User> findAll();

    Optional<User> findById(Long id);

    boolean existsByEmail(String email);

    boolean existsById(Long id);

    User save(User user);

    User saveAndFlush(User user);

    void deleteById(Long id);

    boolean existsByUserName(String userName);
}
