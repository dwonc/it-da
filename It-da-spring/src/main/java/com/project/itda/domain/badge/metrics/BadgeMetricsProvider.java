// src/main/java/com/project/itda/domain/badge/metrics/BadgeMetricsProvider.java
package com.project.itda.domain.badge.metrics;

/**
 * 배지 계산에 필요한 “사용자 지표”를 제공하는 포트(추상화)입니다.
 *
 * 지금은 데이터가 없으므로 NoopBadgeMetricsProvider가 기본값을 반환합니다.
 * 나중에 Participation/Review/Chat 모듈이 생기면, 이 인터페이스를 구현한 Provider를 추가하고 @Primary로 지정하면 됩니다.
 */
public interface BadgeMetricsProvider {

    // 참여/연속/카테고리/시간대
    int getParticipationCompletedCount(Long userId);
    int getCategoryParticipationCompletedCount(Long userId, String categoryParam);
    int getTimeSlotParticipationCompletedCount(Long userId, String timeSlotParam);
    int getConsecutiveDays(Long userId);
    int getConsecutiveWeeks(Long userId);

    // 거리/지역
    int getTotalDistanceKm(Long userId);
    int getDistanceRangeCount(Long userId, double minKm, double maxKm);
    int getDistinctRegionCount(Long userId);

    // 리뷰/평점/감성
    int getReviewCount(Long userId);
    int getPositiveReviewCount(Long userId);
    int getAverageRatingTimes10(Long userId); // 예: 4.8 => 48
    int getPositiveRatePercent(Long userId);  // 예: 95 => 95

    // AI
    int getAiRecommendationParticipationCount(Long userId);
    int getHighSatisfactionParticipationCount(Long userId); // 예측 만족도 기준
    int getAiMatchRatePercent(Long userId);
    int getPredictionAccuracyPercent(Long userId);

    // 소셜/주최
    int getFriendCount(Long userId);
    int getChatCount(Long userId);
    int getHostCount(Long userId);

    // 성향/특별
    int getPersonalityMatchCount(Long userId, String personalityParam);
    int getVibeMatchCount(Long userId, String vibeParam);
    int getSeasonParticipationCount(Long userId, String seasonParam);
    int hasParticipatedOnSpecificDate(Long userId, String dateParam); // YYYY-MM-DD, 참여했으면 1 아니면 0
    int getAllCategoryCompleteCount(Long userId, int minEachCategoryCount);

    /**
     * 첫 로그인 여부(지표).
     * - FIRST_LOGIN 배지에서 사용합니다.
     * - 1이면 "첫 로그인 조건 만족", 0이면 "미만족"
     */
    int isFirstLogin(Long userId);
}
