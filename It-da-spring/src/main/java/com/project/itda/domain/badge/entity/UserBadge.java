// src/main/java/com/project/itda/domain/badge/entity/UserBadge.java
package com.project.itda.domain.badge.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 사용자별 배지 상태 Entity (동적 데이터)
 */
@Entity
@Table(
        name = "user_badges",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_badge", columnNames = {"user_id", "badge_id"})
        },
        indexes = {
                @Index(name = "idx_user_badges_user_id", columnList = "user_id"),
                @Index(name = "idx_user_badges_badge_id", columnList = "badge_id"),
                @Index(name = "idx_user_badges_unlocked", columnList = "unlocked")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userBadgeId;

    @Column(nullable = false, name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    @Column(nullable = false)
    @Builder.Default
    private Boolean unlocked = false;

    @Column(nullable = false)
    @Builder.Default
    private Integer progress = 0;

    private LocalDateTime unlockedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 진행도 업데이트
     */
    public void updateProgress(int newProgress) {
        this.progress = Math.max(0, newProgress);
    }

    /**
     * 배지 획득 처리
     */
    public void unlock() {
        if (Boolean.TRUE.equals(this.unlocked)) {
            return;
        }
        this.unlocked = true;
        this.unlockedAt = LocalDateTime.now();
        this.progress = this.badge.getTargetValue();
    }

    /**
     * 획득 가능 여부
     */
    public boolean canUnlock() {
        return !Boolean.TRUE.equals(unlocked) && progress >= badge.getTargetValue();
    }

    /**
     * 진행률(0~100)
     */
    public double getProgressPercentage() {
        int target = badge.getTargetValue();
        if (target <= 0) {
            return 0.0;
        }
        return Math.min(100.0, (progress * 100.0) / target);
    }
}
