package com.example.post_info.controller;

import com.example.api_response.ApiResponse;
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
    public ResponseEntity<ApiResponse<List<PostResponse>>> getAllPost() {
        List<PostResponse> post = postService.getPost();
        return ResponseEntity.ok(ApiResponse.
                success("Users fetched successfully", post));
    }

    @PostMapping("/user/{userId}/create-post")
    public ResponseEntity<ApiResponse<PostResponse>> createPost(@Valid @RequestBody PostRequest request,
                                                                @PathVariable Long userId) {
        PostResponse newPostResponse = postService.createNewPost(request,userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", newPostResponse));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> dropPost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(@PathVariable Long id,
                                                                @Valid @RequestBody PostRequest postRequest) {
        PostResponse updateResponse = postService.updateUser(id, postRequest);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("User updated successfully", updateResponse));
    }
}
