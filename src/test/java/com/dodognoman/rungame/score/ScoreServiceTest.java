package com.dodognoman.rungame.score;

import com.dodognoman.rungame.score.dto.LeaderboardEntry;
import com.dodognoman.rungame.score.repo.Score;
import com.dodognoman.rungame.score.repo.ScoreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScoreServiceTest {

    @Mock ScoreRepository scoreRepository;
    @InjectMocks ScoreService scoreService;

    private static final Long USER_ID = 1L;

    // ── updateScore ──────────────────────────────────────────────────────────

    @Test
    void updateScore_upsertsAndReturnsNewScore() {
        int result = scoreService.updateScore(USER_ID, 120);

        assertThat(result).isEqualTo(120);
        verify(scoreRepository).upsertScore(USER_ID, 120);
    }

    // ── getScore ─────────────────────────────────────────────────────────────

    @Test
    void getScore_existingRecord_returnsScore() {
        Score existing = new Score();
        existing.setScore(42);
        when(scoreRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existing));

        assertThat(scoreService.getScore(USER_ID)).isEqualTo(42);
    }

    @Test
    void getScore_noRecord_returnsZero() {
        when(scoreRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThat(scoreService.getScore(USER_ID)).isZero();
    }

    // ── leaderboard ──────────────────────────────────────────────────────────

    @Test
    void leaderboard_returnsTopTen() {
        List<LeaderboardEntry> top = List.of(
                new LeaderboardEntry("alice", 300),
                new LeaderboardEntry("bob", 200)
        );
        when(scoreRepository.findLeaderboard(any(Pageable.class))).thenReturn(top);

        List<LeaderboardEntry> result = scoreService.leaderboard(10, 0);

        assertThat(result).isEqualTo(top);
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(scoreRepository).findLeaderboard(captor.capture());
        assertThat(captor.getValue()).isEqualTo(PageRequest.of(0, 10));
    }
}
