package com.example.post_info.service;

import com.example.post_info.dto.PostRequest;
import com.example.post_info.dto.PostResponse;
import com.example.post_info.exception.PostNotExist;
import com.example.post_info.exception.PostNotSave;
import com.example.post_info.model.Post;
import com.example.post_info.repo.PostRepo;

import com.example.user_info.exception.UserNotExist;
import com.example.user_info.model.User;
import com.example.user_info.repo.UserRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {
    private final PostRepo postRepo;
    private final UserRepository userRepository;

    public PostService(PostRepo postRepo, UserRepository userRepository) {
        this.postRepo = postRepo;
        this.userRepository = userRepository;
    }
    @Transactional(readOnly = true)
    public List<PostResponse> getPost() {
        return postRepo.findAll()
                .stream()
                .map(x -> postResponse(x)).collect(Collectors.toList());
    }

    private PostResponse postResponse(Post post) {
        PostResponse postResponse = new PostResponse();
        postResponse.setId(post.getId());
        postResponse.setPostName(post.getPostName());
        postResponse.setContent(post.getPostContent());
        postResponse.setCreated(post.getDateTime());
        postResponse.setUserId(post.getUser() != null ? post.getUser().getId() : null);
        return postResponse;
    }

    @Transactional
    public PostResponse createNewPost(PostRequest request,Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotExist(
                         "User not found with id: " + userId));
        Post savedPost;
        Post post = new Post();
        post.setPostName(request.getPostName());
        post.setPostContent(request.getPostContent());
        post.setDateTime(LocalDateTime.now(ZoneId.of("Asia/Kathmandu")));
        post.setUser(user);
        try {
            savedPost = postRepo.save(post);
        } catch (DataAccessException e) {
            throw new PostNotSave("Fail to save user", e);
        }
        return postResponse(savedPost);
    }

    public void deletePost(Long id) {
        if (!postRepo.existsById(id)) {
            throw new PostNotExist("Id is not exist " + id);
        }
        postRepo.deleteById(id);
    }

    public PostResponse updateUser(Long id, PostRequest postRequest) {
        Post post = postRepo.findById(id)
                .orElseThrow(() -> new PostNotExist("Post not found with id " + id));

        if (postRequest.getPostName() != null && !postRequest.getPostName().isBlank()) {
            post.setPostName(postRequest.getPostName());

        }
        if (postRequest.getPostContent() != null && !postRequest.getPostContent().isBlank()) {
            post.setPostContent(postRequest.getPostContent());
        }
        try {
            return postResponse(postRepo.saveAndFlush(post));
        } catch (DataAccessException e) {
            throw new PostNotSave("Fail to update user", e);
        }
    }
}
