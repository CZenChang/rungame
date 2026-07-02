package com.dodognoman.rungame.score;

import com.dodognoman.rungame.score.dto.LeaderboardEntry;
import com.dodognoman.rungame.score.repo.Score;
import com.dodognoman.rungame.score.repo.ScoreRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ScoreService {

    private static final int LEADERBOARD_SIZE = 10;

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
    public List<LeaderboardEntry> leaderboard() {
        return scoreRepository.findLeaderboard(PageRequest.of(0, LEADERBOARD_SIZE));
    }

    /** 
     * 串流讀取排行榜，並透過 Consumer 回呼消耗資料。
     * JPA Stream 必須在 @Transactional 方法內使用完畢（與資料庫保持連線）。
     */
    @Transactional(readOnly = true)
    public void streamLeaderboard(java.util.function.Consumer<LeaderboardEntry> consumer) {
        try (java.util.stream.Stream<LeaderboardEntry> stream = scoreRepository.streamLeaderboard()) {
            stream.forEach(consumer);
        }
    }
}
