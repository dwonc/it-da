// src/main/java/com/project/itda/domain/badge/enums/BadgeConditionType.java
package com.project.itda.domain.badge.enums;

/**
 * 조건 타입 (20종)
 * 실제 계산은 BadgeMetricsProvider에서 제공하는 “지표 값”을 이용해 계산합니다.
 */
public enum BadgeConditionType {
    // 횟수 기반
    PARTICIPATION_COUNT,
    CATEGORY_COUNT,
    TIME_SLOT_COUNT,
    REVIEW_COUNT,
    POSITIVE_REVIEW_COUNT,
    HOST_COUNT,
    FRIEND_COUNT,
    CHAT_COUNT,

    // 연속 기반
    CONSECUTIVE_DAYS,
    CONSECUTIVE_WEEKS,

    // 거리 기반
    TOTAL_DISTANCE,
    DISTANCE_RANGE_COUNT,
    REGION_COUNT,

    // 평점/만족도 기반
    AVERAGE_RATING,
    AI_MATCH_RATE,
    PREDICTION_ACCURACY,
    POSITIVE_RATE,

    // AI 기반
    AI_RECOMMENDATION_COUNT,
    HIGH_SATISFACTION_COUNT,

    // 성향 기반
    PERSONALITY_MATCH_COUNT,
    VIBE_MATCH_COUNT,

    // 특별
    FIRST_LOGIN,              // ⭐⭐⭐ 이 줄 추가
    SPECIFIC_DATE,
    SEASON_PARTICIPATION,
    ALL_CATEGORY_COMPLETE
}
