package com.example.post;

import com.example.post_info.controller.PostController;
import com.example.post_info.dto.PostRequest;
import com.example.post_info.dto.PostResponse;
import com.example.post_info.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;

@WebMvcTest(PostController.class)
class PostControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;
    @Test
    void getAllPost_whenPostsExist_returns200WithList() throws Exception {
        List<PostResponse> responses = List.of(
                new PostResponse(1L, "Exciting Day", "Today was a wonderful day!"),
                new PostResponse(2L, "Planning Avoiding", "I had a terrible day today.")
        );

        when(postService.getPost()).thenReturn(responses);

        mockMvc.perform(get("/post/get-allPosts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].postName").value("Exciting Day"))
                .andExpect(jsonPath("$[0].content").value("Today was a wonderful day!"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].postName").value("Planning Avoiding"))
                .andExpect(jsonPath("$[1].content").value("I had a terrible day today."));

        verify(postService).getPost();
    }

    @Test
    void getAllPost_whenNoPosts_returns200WithEmptyList() throws Exception {
        when(postService.getPost()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/post/get-allPosts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(postService).getPost();
    }
    @Test
    void createPost_validRequest_returns201WithCreatedPost() throws Exception {
        PostRequest request = new PostRequest("Exciting Day", "Today was a wonderful day!");
        PostResponse savedResponse = new PostResponse(1L, "Exciting Day", "Today was a wonderful day!");

        when(postService.createNewPost(any(PostRequest.class))).thenReturn(savedResponse);

        mockMvc.perform(post("/post/create-post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.postName").value("Exciting Day"))
                .andExpect(jsonPath("$.content").value("Today was a wonderful day!"));

        verify(postService).createNewPost(any(PostRequest.class));
    }

    @Test
    void createPost_blankName_returns400AndDoesNotCallService() throws Exception {
        PostRequest request = new PostRequest("", "Today was a wonderful day!");

        mockMvc.perform(post("/post/create-post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(postService);
    }
}
