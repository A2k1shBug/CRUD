package com.example.user_info.repo;


import com.example.user_info.model.PostUser;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaUserDao implements UserDao {

    // Spring le shared EntityManager inject garcha (thread-safe proxy)
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<PostUser> findAll() {
        // JPQL ma table ko naam hoina, Entity class ko naam (User) lekhincha
        return entityManager
                .createQuery("SELECT u FROM PostUser u ORDER BY u.id", PostUser.class)
                .getResultList();
    }

    @Override
    public Optional<PostUser> findById(Long id) {
        // find() le primary key bata row lincha, na paye null
        return Optional.ofNullable(entityManager.find(PostUser.class, id));
    }

    @Override
    public boolean existsByEmail(String email) {
        // Row count ko matra check garcha, entity load gardaina
        Long count = entityManager
                .createQuery("SELECT COUNT(u) FROM PostUser u WHERE u.email = :email", Long.class)
                .setParameter("email", email)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public boolean existsById(Long id) {
        Long count = entityManager
                .createQuery("SELECT COUNT(u) FROM PostUser u WHERE u.id = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult();
        return count > 0;
    }

    @Override
    @Transactional
    public PostUser save(PostUser user) {
        if (user.getId() == null) {
            // Naya user: persist le managed state ma rakhcha
            entityManager.persist(user);
            return user;
        }
        // Existing user: merge le DB row update garcha
        return entityManager.merge(user);
    }

    @Override
    @Transactional
    public PostUser saveAndFlush(PostUser user) {
        PostUser saved = save(user);
        // flush le SQL turuntai DB ma pathauchha.
        // Unique constraint error yahi aaucha, commit pachhi haina.
        entityManager.flush();
        return saved;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        PostUser user = entityManager.find(PostUser.class, id);
        if (user != null) {
            // Posts cascade=ALL ra orphanRemoval=true chha, tesaile posts pani delete huncha
            entityManager.remove(user);
        }
    }

    @Override
    public boolean existsByUserName(String name) {
        Long count =
                entityManager
                        .createQuery("SELECT COUNT(u) FROM PostUser u WHERE u.name = :name", Long.class)
                        .setParameter("name", name)
                        .getSingleResult();
        return count > 0;

    }

    @Override
    public PostUser findByName(String username) {
        List<PostUser> result = entityManager
                .createQuery("SELECT u FROM PostUser u WHERE u.name = :name", PostUser.class)
                .setParameter("name", username)
                .setMaxResults(1)
                .getResultList();
        return result.isEmpty() ? null : result.get(0);
    }
}