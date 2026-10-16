package com.example.post;

import com.example.post_info.controller.PostController;
import com.example.post_info.dto.PostRequest;
import com.example.post_info.dto.PostResponse;
import com.example.post_info.exception.PostNotExist;
import com.example.post_info.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    //Get
    @Test
    void getAllPost_whenPostsExist_returns200WithList() throws Exception {

        // ARRANGE: service le return garne fake data
        List<PostResponse> responses = List.of(
                new PostResponse(1L, "Exciting Day",
                        "Today was a wonderful day!", LocalDateTime.now(ZoneId.of("Asia/Kathmandu")),1L),
                new PostResponse(2L, "Planning Avoiding",
                        "I had a terrible day today.",LocalDateTime.now(ZoneId.of("Asia/Kathmandu")),2L)
        );
        when(postService.getPost()).thenReturn(responses);

        // ACT + ASSERT
        mockMvc.perform(get("/post/get-allPosts"))
                .andExpect(status().isOk())

                // wrapper ko message check garne
                .andExpect(jsonPath("$.message").value("Users fetched successfully"))

                // list $.data bhitra cha, tesaile $.data.length()
                .andExpect(jsonPath("$.data.length()").value(2))

                // $.data[0] = list ko pahilo item
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].postName").value("Exciting Day"))
                .andExpect(jsonPath("$.data[0].content").value("Today was a wonderful day!"))

                // $.data[1] = dosro item
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[1].postName").value("Planning Avoiding"))
                .andExpect(jsonPath("$.data[1].content").value("I had a terrible day today."));

        verify(postService).getPost();
    }

    @Test
    void getAllPost_whenNoPosts_returns200WithEmptyList() throws Exception {
        when(postService.getPost()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/post/get-allPosts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(postService).getPost();
    }

    // Post
    @Test
    void createPost_validRequest_returns201WithCreatedPost() throws Exception {

        // ARRANGE: request body ra service le return garne fake response
        PostRequest request = new PostRequest("Exciting Day", "Today was a wonderful day!");
        PostResponse savedResponse = new PostResponse(1L, "Exciting Day",
                "Today was a wonderful day!",LocalDateTime.now(ZoneId.of("Asia/Kathmandu")),1L);

        // service.createNewPost(...) call bhayo bhane savedResponse return gara
        when(postService.createNewPost( any(PostRequest.class),anyLong())).thenReturn(savedResponse);

        // ACT: JSON body sahit POST request pathaune
        mockMvc.perform(post("/post/create-post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // ASSERT
                .andExpect(status().isCreated())                          // 201
                .andExpect(jsonPath("$.data.id").value(1))                // $.id haina, $.data.id
                .andExpect(jsonPath("$.data.postName").value("Exciting Day"))
                .andExpect(jsonPath("$.data.content").value("Today was a wonderful day!"));

        verify(postService).createNewPost( any(PostRequest.class),anyLong());
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

    //Delete
    @Test
    void dropPost_existingId_returns204AndCallsService() throws Exception {

        //  deletePost() void ho, tesaile when(...) chaidaina.
        // Mockito mock le void method ma kehi gardaina, exception pani falaidaina.

        // DELETE /post/delete/1
        mockMvc.perform(delete("/post/delete/{id}", 1L))

                // ASSERT: 204 No Content, body khali hunu parcha
                .andExpect(status().isNoContent());

        // {id} URL bata Long ma convert bhayera service samma 1L pugyo ki?
        verify(postService).deletePost(1L);
    }

    @Test
    void dropPost_idNotFound_returns404() throws Exception {

        // ARRANGE: void method ma thenThrow() chaldaina, doThrow().when() use garnu parcha.
        // ResourceNotFoundException = tapai ko service le post nabhetda falne exception. Naam milaunus.
        doThrow(new PostNotExist("Post not found with id 99"))
                .when(postService).deletePost(99L);

        // ACT + ASSERT: @RestControllerAdvice le exception lai 404 ma convert garcha
        mockMvc.perform(delete("/post/delete/{id}", 99L))
                .andExpect(status().isNotFound());

        verify(postService).deletePost(99L);
    }
    @Test
    void dropPost_invalidIdFormat_returns400AndDoesNotCallService() throws Exception {

        // "abc" lai Long ma convert garna sakdaina, Spring le service samma pugna dindaina
        mockMvc.perform(delete("/post/delete/{id}", "abc"))
                .andExpect(status().isBadRequest());

        // Controller method nai run bhayena, so service call hunu hudaina
        verify(postService, never()).deletePost(any());
    }
    //update
    @Test
    void updatePost_validRequest_returns200WithUpdatedPost() throws Exception {

        //  update garne data ra service le return garne response
        PostRequest request = new PostRequest("Updated Day Title", "Updated content of the post");
        PostResponse updated = new PostResponse(1L, "Updated Day Title",
                "Updated content of the post",LocalDateTime.now(ZoneId.of("Asia/Kathmandu")),1L);

        // eq(1L) = id exactly 1 hunu parcha. Mockito ma ek argument matcher use gare
        // sabai argument matcher nai hunu parcha, tesaile id ma pani eq() halyeko.
        when(postService.updateUser(eq(1L), any(PostRequest.class))).thenReturn(updated);

        //PUT /post/update/1 with JSON body
        mockMvc.perform(put("/post/update/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // ASSERT
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.postName").value("Updated Day Title"))
                .andExpect(jsonPath("$.data.content").value("Updated content of the post"));

        // JSON body ra path id sahi service lai pugyo ki, captor le check garne
        ArgumentCaptor<PostRequest> captor = ArgumentCaptor.forClass(PostRequest.class);
        verify(postService).updateUser(eq(1L), captor.capture());
        assertEquals("Updated Day Title", captor.getValue().getPostName());
        assertEquals("Updated content of the post", captor.getValue().getPostContent());
    }
    @Test
    void updatePost_idNotFound_returns404() throws Exception {

        //  valid request, tara id 99 database ma chaina
        PostRequest request = new PostRequest("Updated Day Title", "Updated content of the post");

        // updateUser() value return garcha (void haina), so thenThrow() chalcha
        when(postService.updateUser(eq(99L), any(PostRequest.class)))
                .thenThrow(new PostNotExist("Post not found with id 99"));

        // ACT + ASSERT: @RestControllerAdvice le exception lai 404 ma convert garcha
        mockMvc.perform(put("/post/update/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePost_blankName_returns400AndDoesNotCallService() throws Exception {

        // ARRANGE: postName khali (@NotBlank le rokchha)
        PostRequest invalid = new PostRequest("", "Updated content of the post");

        // ACT + ASSERT
        mockMvc.perform(put("/post/update/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        // validation fail bhayo, service samma pugnu hudaina
        verifyNoInteractions(postService);
    }

    @Test
    void updatePost_contentTooShort_returns400AndDoesNotCallService() throws Exception {

        // ARRANGE: content 10 character bhanda sano (@Size(min = 10))
        PostRequest invalid = new PostRequest("Updated Day Title", "short");

        mockMvc.perform(put("/post/update/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(postService);
    }

    @Test
    void updatePost_missingBody_returns400AndDoesNotCallService() throws Exception {

        // ACT: body nai pathaudainau. @RequestBody required hunchha
        mockMvc.perform(put("/post/update/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(postService);
    }
}
