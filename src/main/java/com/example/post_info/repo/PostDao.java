package com.example.post_info.repo;

import com.example.post_info.model.Post;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostDao  {
    List<Post> findAll();

    Optional<Post> findById(Long id);

    boolean existsById(Long id);

    Post save(Post post);

    Post saveAndFlush(Post post);

    void deleteById(Long id);
}
