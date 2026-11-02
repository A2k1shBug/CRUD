package com.example.user_dao_test;


import com.example.user_info.model.User;
import com.example.user_info.repo.JpaUserDao;
import com.example.user_info.repo.UserDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.dao.PersistenceExceptionTranslationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// DataJpaTest le JPA components matra load garcha.
// @Repository class ko lagi @Import le JpaUserDao lai context ma halna parcha.
@DataJpaTest
@Import({JpaUserDao.class, PersistenceExceptionTranslationAutoConfiguration.class})
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class JpaUserDaoTest {

    @Autowired
    private UserDao userDao;

    // Direct entity operation ko lagi. Flush/clear garera DB bata read garna help garcha.
    @Autowired
    private TestEntityManager em;

    private User newUser(String email, String phone) {
        User user = new User();
        user.setName("Test User");
        user.setEmail(email);
        user.setPassword("secret123");
        user.setPhoneNumber(phone);
        return user;
    }

    @Test
    void save_newUser_assignsId() {
        User saved = userDao.save(newUser("a@mail.com", "9800000001"));

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void findById_existingUser_returnsUser() {
        User saved = userDao.save(newUser("a@mail.com", "9800000001"));
        em.flush();
        em.clear(); // cache hatauera DB bata matra padhcha

        var found = userDao.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("a@mail.com");
    }

    @Test
    void findById_missingUser_returnsEmpty() {
        assertThat(userDao.findById(999L)).isEmpty();
    }

    @Test
    void findAll_returnsAllUsers() {
        userDao.save(newUser("a@mail.com", "9800000001"));
        userDao.save(newUser("b@mail.com", "9800000002"));
        em.flush();
        em.clear();

        List<User> users = userDao.findAll();

        assertThat(users).hasSize(2);
    }

    @Test
    void existsByEmail_trueForRegisteredEmail_falseOtherwise() {
        userDao.save(newUser("a@mail.com", "9800000001"));
        em.flush();

        assertThat(userDao.existsByEmail("a@mail.com")).isTrue();
        assertThat(userDao.existsByEmail("x@mail.com")).isFalse();
    }

    @Test
    void existsById_trueForExistingId_falseOtherwise() {
        User saved = userDao.save(newUser("a@mail.com", "9800000001"));
        em.flush();

        assertThat(userDao.existsById(saved.getId())).isTrue();
        assertThat(userDao.existsById(999L)).isFalse();
    }

    @Test
    void save_existingUser_updatesRow() {
        User saved = userDao.save(newUser("a@mail.com", "9800000001"));
        em.flush();
        em.clear();

        User toUpdate = userDao.findById(saved.getId()).orElseThrow();
        toUpdate.setName("Updated Name");
        userDao.save(toUpdate);
        em.flush();
        em.clear();

        User fromDb = em.find(User.class, saved.getId());
        assertThat(fromDb.getName()).isEqualTo("Updated Name");
    }

    @Test
    void deleteById_removesUser() {
        User saved = userDao.save(newUser("a@mail.com", "9800000001"));
        em.flush();
        em.clear();

        userDao.deleteById(saved.getId());
        em.flush();
        em.clear();

        assertThat(userDao.findById(saved.getId())).isEmpty();
    }

    @Test
    void saveAndFlush_duplicateEmail_throwsDataAccessException() {
        userDao.save(newUser("a@mail.com", "9800000001"));
        em.flush();

        // Email unique cha. Duplicate ma flush garda DB error aauchha,
        // @Repository le use garera DataAccessException ma convert garcha.
        assertThatThrownBy(() -> userDao.saveAndFlush(newUser("a@mail.com", "9800000002")))
                .isInstanceOf(DataAccessException.class);
    }
}
