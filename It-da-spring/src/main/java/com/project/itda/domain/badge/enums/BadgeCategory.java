// src/main/java/com/project/itda/domain/badge/enums/BadgeCategory.java
package com.project.itda.domain.badge.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 배지 카테고리 (10종)
 */
@Getter
@RequiredArgsConstructor
public enum BadgeCategory {
    PARTICIPATION("참여"),
    AI("AI 추천"),
    DISTANCE("거리"),
    TIME("시간대"),
    PERSONALITY("성향"),
    CATEGORY("카테고리"),
    REVIEW("리뷰/평점"),
    SOCIAL("소셜"),
    HOST("주최"),
    SPECIAL("특별");

    private final String displayName;
}
