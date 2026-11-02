package com.example.post_info.repo;

import com.example.post_info.model.Post;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaPostDao implements PostDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Post> findAll() {
        // Entity ko naam (Post) use garcha, table ko naam (posts) hoina
        return entityManager
                .createQuery("SELECT p FROM Post p ORDER BY p.id", Post.class)
                .getResultList();
    }

    @Override
    public Optional<Post> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Post.class, id));
    }

    @Override
    public boolean existsById(Long id) {
        // Row count matra check garcha, entity load gardaina
        Long count = entityManager
                .createQuery("SELECT COUNT(p) FROM Post p WHERE p.id = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult();
        return count > 0;
    }

    @Override
    @Transactional
    public Post save(Post post) {
        if (post.getId() == null) {
            // Naya post: persist le managed state ma rakhcha
            entityManager.persist(post);
            return post;
        }
        // Existing post: merge le DB row update garcha
        return entityManager.merge(post);
    }

    @Override
    @Transactional
    public Post saveAndFlush(Post post) {
        Post saved = save(post);
        // flush le SQL turuntai DB ma pathauchha, error yahi aauchha
        entityManager.flush();
        return saved;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Post post = entityManager.find(Post.class, id);
        if (post != null) {
            entityManager.remove(post);
        }
    }
}
