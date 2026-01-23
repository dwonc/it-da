// src/main/java/com/project/itda/domain/badge/entity/Badge.java
package com.project.itda.domain.badge.entity;

import com.project.itda.domain.badge.enums.BadgeCategory;
import com.project.itda.domain.badge.enums.BadgeConditionType;
import com.project.itda.domain.badge.enums.BadgeGrade;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 배지 정의 Entity (정적 데이터)
 */
@Entity
@Table(
        name = "badges",
        indexes = {
                @Index(name = "idx_badge_condition_type", columnList = "condition_type"),
                @Index(name = "idx_badge_category", columnList = "category")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long badgeId;

    @Column(nullable = false, unique = true, length = 50)
    private String badgeCode;

    @Column(nullable = false, length = 100)
    private String badgeName;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BadgeGrade grade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BadgeCategory category;

    @Column(length = 10)
    private String icon;

    @Column(nullable = false)
    private Integer targetValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, name = "condition_type")
    private BadgeConditionType conditionType;

    @Column(length = 50, name = "condition_param")
    private String conditionParam;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
