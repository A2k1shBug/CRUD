package com.example.post_info.controller;

import com.example.post_info.dto.PostRequest;
import com.example.post_info.dto.PostResponse;
import com.example.post_info.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/post")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/get-allPosts")
    public ResponseEntity<List<PostResponse>> getAllPost() {
        List<PostResponse> post = postService.getPost();
        return new ResponseEntity<>(post, HttpStatus.OK);
    }

    @PostMapping("/create-post")
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostRequest request) {
        PostResponse newPostResponse = postService.createNewPost(request);
        return new ResponseEntity<>(newPostResponse, HttpStatus.CREATED);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> dropPost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<PostResponse> updatePost(@PathVariable Long id, @RequestBody PostRequest postRequest) {
        PostResponse postResponse = postService.updateUser(id, postRequest);
        return new ResponseEntity<>(postResponse,HttpStatus.OK);
    }
}
