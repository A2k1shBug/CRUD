package com.example.post;

import com.example.post_info.dto.PostResponse;
import com.example.post_info.model.Post;
import com.example.post_info.repo.PostRepo;
import com.example.post_info.service.PostService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {
    @Mock
    private PostRepo postRepo;
    @InjectMocks
    private PostService postService;

    @Test
    void getPost_whenPostsExist_returnsMappedResponses() {

        // ARRANGE: duita alag Post object banaune (har setter sahi variable ma)
        Post post1 = new Post();
        post1.setId(1L);
        post1.setPostName("Exciting Day");
        post1.setPostContent("Today was a wonderful day!");

        Post post2 = new Post();
        post2.setId(2L);
        post2.setPostName("Planning Avoiding");
        post2.setPostContent("I had a terrible day today.");

        // repository.findAll() call bhayo bhane yehi list return gara
        when(postRepo.findAll()).thenReturn(List.of(post1, post2));

        // ACT: test garna parne method call garne
        List<PostResponse> responses = postService.getPost();

        // ASSERT: Post -> PostResponse mapping thik cha ki?
        assertEquals(2, responses.size());

        // Pahilo item (repository ko order maintain huncha, kinabhane stream le order change gardaina)
        assertEquals(post1.getId(), responses.get(0).getId());
        assertEquals(post1.getPostName(), responses.get(0).getPostName());
        assertEquals(post1.getPostContent(), responses.get(0).getContent());

        // Dosro item
        assertEquals(post2.getId(), responses.get(1).getId());
        assertEquals(post2.getPostName(), responses.get(1).getPostName());
        assertEquals(post2.getPostContent(), responses.get(1).getContent());

        // Service le repository lai exactly ek choti call garyo ki?
        verify(postRepo).findAll();
    }

    @Test
    void getPost_whenNoPosts_returnsEmptyList() {

        // ARRANGE: database khali cha bhanera pretend garne
        when(postRepo.findAll()).thenReturn(Collections.emptyList());

        // ACT
        List<PostResponse> responses = postService.getPost();

        // ASSERT: null haina, empty list aunu parcha (null aayo bhane controller ma NullPointerException ko risk huncha)
        assertTrue(responses.isEmpty());

        verify(postRepo).findAll();
    }

    @Test
        //Testing reponse CreateNew Post
    void getUser_returnsAllUsersAsResponseDtos() {
        Post post1 = new Post();
        post1.setId(1L);
        post1.setPostName("Exciting Day");
        post1.setPostContent("Today was a wonderful day! I completed my project successfully.");

        Post post2 = new Post();
        post2.setId(2L);
        post2.setPostName("Planning Avoiding");
        post2.setPostContent("I had a terrible day today. Nothing went according to my plan.");

        when(postRepo.findAll()).thenReturn(List.of(post1, post2));

        List<PostResponse> responses = postService.getPost();

        assertEquals(2, responses.size());

        assertEquals(post1.getPostName(), responses.get(0).getPostName());
        assertEquals(post1.getPostContent(), responses.get(0).getContent());

        assertEquals(post2.getPostName(), responses.get(1).getPostName());
        assertEquals(post2.getPostContent(), responses.get(1).getContent());

        verify(postRepo).findAll();
    }
}


