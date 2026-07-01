package com.dodognoman.rungame.score.repo;

import com.dodognoman.rungame.score.dto.LeaderboardEntry;
import com.dodognoman.rungame.user.repo.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:tc:postgresql:17-alpine:///rungame",
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
        "spring.main.lazy-initialization=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ScoreRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ScoreRepository scoreRepository;

    @Test
    void whenFindByUserId_thenReturnScore() {
        // given
        User user = new User();
        user.setUsername("player1");
        user.setPasswordHash("hash");
        user.setRegisterIp("127.0.0.1");
        user.setLastLoginIp("127.0.0.1");
        user = entityManager.persist(user);

        Score score = new Score();
        score.setUser(user);
        score.setScore(100);
        entityManager.persist(score);
        entityManager.flush();

        // when
        Optional<Score> found = scoreRepository.findByUserId(user.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getScore()).isEqualTo(100);
    }

    @Test
    void testUpsertScore() {
        // given
        User user = new User();
        user.setUsername("player2");
        user.setPasswordHash("hash");
        user.setRegisterIp("127.0.0.1");
        user.setLastLoginIp("127.0.0.1");
        user = entityManager.persist(user);
        entityManager.flush();

        // when - insert
        scoreRepository.upsertScore(user.getId(), 50);

        // then
        Optional<Score> found = scoreRepository.findByUserId(user.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getScore()).isEqualTo(50);

        // when - update
        scoreRepository.upsertScore(user.getId(), 200);

        // then
        found = scoreRepository.findByUserId(user.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getScore()).isEqualTo(200);
    }

    @Test
    void testFindLeaderboard() {
        // given
        User u1 = new User(); u1.setUsername("u1"); u1.setPasswordHash("h"); u1.setRegisterIp("1.1.1.1"); u1.setLastLoginIp("1.1.1.1");
        User u2 = new User(); u2.setUsername("u2"); u2.setPasswordHash("h"); u2.setRegisterIp("1.1.1.1"); u2.setLastLoginIp("1.1.1.1");
        u1 = entityManager.persist(u1);
        u2 = entityManager.persist(u2);

        Score s1 = new Score(); s1.setUser(u1); s1.setScore(100);
        Score s2 = new Score(); s2.setUser(u2); s2.setScore(300);
        entityManager.persist(s1);
        entityManager.persist(s2);
        entityManager.flush();

        // when
        List<LeaderboardEntry> leaderboard = scoreRepository.findLeaderboard(PageRequest.of(0, 10));

        // then
        assertThat(leaderboard).hasSize(2);
        assertThat(leaderboard.get(0).username()).isEqualTo("u2");
        assertThat(leaderboard.get(0).score()).isEqualTo(300);
        assertThat(leaderboard.get(1).username()).isEqualTo("u1");
        assertThat(leaderboard.get(1).score()).isEqualTo(100);
    }
}
