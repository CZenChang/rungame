package com.dodognoman.rungame.score;

import com.dodognoman.rungame.score.dto.LeaderboardEntry;
import com.dodognoman.rungame.score.repo.Score;
import com.dodognoman.rungame.score.repo.ScoreRepository;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
public class ScoreService {

    private final ScoreRepository scoreRepository;

    public ScoreService(ScoreRepository scoreRepository) {
        this.scoreRepository = scoreRepository;
    }

    /**
     * 更新（覆蓋）使用者分數，回傳更新後的分數。
     * 以 upsert 一次完成新增或覆蓋，第一次更新時自動建立該 user 的分數紀錄。
     * 覆蓋語意下更新後的分數即為傳入值，無須再讀回。
     */
    @Transactional
    public int updateScore(Long userId, int newScore) {
        scoreRepository.upsertScore(userId, newScore);
        return newScore;
    }

    /** 查詢使用者分數，尚無紀錄則回傳 0。 */
    @Transactional(readOnly = true)
    public int getScore(Long userId) {
        return scoreRepository.findByUserId(userId)
                .map(Score::getScore)
                .orElse(0);
    }

    /** 排行榜前十名。 */
    @Transactional(readOnly = true)
    public List<LeaderboardEntry> leaderboard(@Min(1) int size , int page) {
        return scoreRepository.findLeaderboard(PageRequest.of(page, size));
    }
}
