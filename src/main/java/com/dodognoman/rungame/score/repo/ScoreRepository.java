package com.dodognoman.rungame.score.repo;

import com.dodognoman.rungame.score.dto.LeaderboardEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScoreRepository extends JpaRepository<Score, Long> {
    Optional<Score> findByUserId(Long userId);
    boolean existsByUserId(Long userId);

    /**
     * 以原生 SQL 一次完成「新增或覆蓋」分數（upsert）。
     *
     * <p>相較於「先 findByUserId 再 save」省去一次 SELECT，且為原子操作——
     * 兩個請求同時對同一 user 首次寫入時，不會雙雙 INSERT 撞 UNIQUE 約束。
     * 依賴 scores.fk_user_id 的 UNIQUE 約束來觸發 ON CONFLICT。
     */
    @Modifying
    @Query(value = """
            INSERT INTO scores (fk_user_id, score, created_at, updated_at)
            VALUES (:userId, :score, now(), now())
            ON CONFLICT (fk_user_id)
            DO UPDATE SET score = EXCLUDED.score, updated_at = now()
            """, nativeQuery = true)
    void upsertScore(@Param("userId") Long userId, @Param("score") int score);

    // 排行榜：以 username + score 投影，單一 JOIN 查詢，不會載入整個 User 實體
    @Query("""
            select new com.dodognoman.rungame.score.dto.LeaderboardEntry(s.user.username, s.score)
            from Score s
            order by s.score desc
            """)
    List<LeaderboardEntry> findLeaderboard(Pageable pageable);

    // 串流讀取排行榜：使用 Stream 與 Fetch Size 避免 OOM
    @org.springframework.data.jpa.repository.QueryHints(
            @jakarta.persistence.QueryHint(name = "org.hibernate.fetchSize", value = "50")
    )
    @Query("""
            select new com.dodognoman.rungame.score.dto.LeaderboardEntry(s.user.username, s.score)
            from Score s
            order by s.score desc
            """)
    java.util.stream.Stream<LeaderboardEntry> streamLeaderboard();
}
