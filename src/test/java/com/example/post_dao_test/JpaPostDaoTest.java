package com.example.post_dao_test;

import com.example.post_info.model.Post;
import com.example.post_info.repo.JpaPostDao;
import com.example.post_info.repo.PostDao;
import com.example.user_info.model.User;
import com.example.user_info.repo.JpaUserDao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaPostDao.class, JpaUserDao.class})
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class JpaPostDaoTest {

    @Autowired
    private PostDao postDao;

    @Autowired
    private TestEntityManager em;

    private User owner;

    @BeforeEach
    void setUp() {
        // Har test ko lagi ek user (post ko owner)
        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@mail.com");
        owner.setPassword("secret123");
        owner.setPhoneNumber("9800000001");
        em.persist(owner);
        em.flush();
    }

    private Post newPost(String name, String content) {
        Post post = new Post();
        post.setPostName(name);
        post.setPostContent(content);
        post.setDateTime(LocalDateTime.now());
        post.setUser(owner);
        return post;
    }

    @Test
    void save_newPost_assignsId() {
        Post saved = postDao.save(newPost("First Post", "This is the content"));

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void findById_existingPost_returnsPostWithUser() {
        Post saved = postDao.save(newPost("First Post", "This is the content"));
        em.flush();
        em.clear();

        Post found = postDao.findById(saved.getId()).orElseThrow();

        assertThat(found.getPostName()).isEqualTo("First Post");
        assertThat(found.getUser().getId()).isEqualTo(owner.getId());
    }

    @Test
    void findById_missingPost_returnsEmpty() {
        assertThat(postDao.findById(999L)).isEmpty();
    }

    @Test
    void findAll_returnsAllPosts() {
        postDao.save(newPost("First Post", "This is the content"));
        postDao.save(newPost("Second Post", "Another content here"));
        em.flush();
        em.clear();

        List<Post> posts = postDao.findAll();

        assertThat(posts).hasSize(2);
    }

    @Test
    void existsById_trueForExistingId_falseOtherwise() {
        Post saved = postDao.save(newPost("First Post", "This is the content"));
        em.flush();

        assertThat(postDao.existsById(saved.getId())).isTrue();
        assertThat(postDao.existsById(999L)).isFalse();
    }

    @Test
    void saveAndFlush_existingPost_updatesContent() {
        Post saved = postDao.save(newPost("First Post", "Old content here"));
        em.flush();
        em.clear();

        Post toUpdate = postDao.findById(saved.getId()).orElseThrow();
        toUpdate.setPostContent("New content here");
        postDao.saveAndFlush(toUpdate);
        em.clear();

        Post fromDb = em.find(Post.class, saved.getId());
        assertThat(fromDb.getPostContent()).isEqualTo("New content here");
    }

    @Test
    void deleteById_removesPost_butKeepsUser() {
        Post saved = postDao.save(newPost("First Post", "This is the content"));
        em.flush();
        em.clear();

        postDao.deleteById(saved.getId());
        em.flush();
        em.clear();

        assertThat(postDao.findById(saved.getId())).isEmpty();
        // Post delete bhayo, user delete hunu hudaina
        assertThat(em.find(User.class, owner.getId())).isNotNull();
    }
}
