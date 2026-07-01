package com.dodognoman.rungame.user.repo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:tc:postgresql:17-alpine:///rungame",
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void whenFindByUsername_thenReturnUser() {
        // given
        User user = new User();
        user.setUsername("testuser");
        user.setPasswordHash("hash123");
        user.setRegisterIp("127.0.0.1");
        user.setLastLoginIp("127.0.0.1");
        entityManager.persist(user);
        entityManager.flush();

        // when
        Optional<User> found = userRepository.findByUsername(user.getUsername());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo(user.getUsername());
    }

    @Test
    void whenExistsByUsername_thenReturnTrue() {
        // given
        User user = new User();
        user.setUsername("existuser");
        user.setPasswordHash("hash123");
        user.setRegisterIp("127.0.0.1");
        user.setLastLoginIp("127.0.0.1");
        entityManager.persist(user);
        entityManager.flush();

        // when
        boolean exists = userRepository.existsByUsername(user.getUsername());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void whenFindByAccessToken_thenReturnUser() {
        // given
        User user = new User();
        user.setUsername("tokenuser");
        user.setPasswordHash("hash123");
        user.setAccessToken("valid-token");
        user.setRegisterIp("127.0.0.1");
        user.setLastLoginIp("127.0.0.1");
        entityManager.persist(user);
        entityManager.flush();

        // when
        Optional<User> found = userRepository.findByAccessToken("valid-token");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo(user.getUsername());
    }
}
