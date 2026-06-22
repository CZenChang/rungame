package com.dodognoman.rungame.score.repo;

import com.dodognoman.rungame.common.BaseEntity;
import com.dodognoman.rungame.user.repo.User;
import jakarta.persistence.*;

@Entity
@Table(name = "scores")
public class Score extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 邏輯外鍵，對應 users(id)。
     * - LAZY：撈 Score 時不會一併撈出 User，需存取時才查詢。
     * - NO_CONSTRAINT：DB 不建立實體外鍵約束，關聯僅由 JPA 維護。
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "fk_user_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private User user;

    @Column(nullable = false)
    private int score = 0;

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}
