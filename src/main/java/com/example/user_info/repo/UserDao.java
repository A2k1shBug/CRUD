package com.example.user_info.repo;

import com.example.user_info.model.PostUser;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDao {
    List<PostUser> findAll();

    Optional<PostUser> findById(Long id);

    boolean existsByEmail(String email);

    boolean existsById(Long id);

    PostUser save(PostUser user);

    PostUser saveAndFlush(PostUser user);

    void deleteById(Long id);

    boolean existsByUserName(String userName);

    PostUser findByName(String username);
}
