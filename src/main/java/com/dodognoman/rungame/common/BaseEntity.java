package com.dodognoman.rungame.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * @author Ceizer
 * @apiNote
 * @since 2026/6/17
 */
@MappedSuperclass // 💡 聲明這是父類別，欄位會映射到子類別的資料表中
@EntityListeners(AuditingEntityListener.class) // 💡 監聽實體狀態，觸發自動填入
public class BaseEntity {
    @CreatedDate // 💡 插入資料時，JPA 會自動填入當前時間
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate // 💡 更新資料時，JPA 會自動填入當前時間
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}