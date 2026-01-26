// src/main/java/com/project/itda/domain/badge/metrics/NoopBadgeMetricsProvider.java
package com.project.itda.domain.badge.metrics;

import org.springframework.stereotype.Component;

/**
 * 데이터가 아직 없는 단계에서 사용하는 기본 Provider.
 * ⚠️ @Primary 제거됨 - RealBadgeMetricsProvider가 우선 사용됨
 */
@Component
public class NoopBadgeMetricsProvider implements BadgeMetricsProvider {

    @Override
    public int getParticipationCompletedCount(Long userId) { return 0; }

    @Override
    public int getCategoryParticipationCompletedCount(Long userId, String categoryParam) { return 0; }

    @Override
    public int getTimeSlotParticipationCompletedCount(Long userId, String timeSlotParam) { return 0; }

    @Override
    public int getConsecutiveDays(Long userId) { return 0; }

    @Override
    public int getConsecutiveWeeks(Long userId) { return 0; }

    @Override
    public int getTotalDistanceKm(Long userId) { return 0; }

    @Override
    public int getDistanceRangeCount(Long userId, double minKm, double maxKm) { return 0; }

    @Override
    public int getDistinctRegionCount(Long userId) { return 0; }

    @Override
    public int getReviewCount(Long userId) { return 0; }

    @Override
    public int getPositiveReviewCount(Long userId) { return 0; }

    @Override
    public int getAverageRatingTimes10(Long userId) { return 0; }

    @Override
    public int getPositiveRatePercent(Long userId) { return 0; }

    @Override
    public int getAiRecommendationParticipationCount(Long userId) { return 0; }

    @Override
    public int getHighSatisfactionParticipationCount(Long userId) { return 0; }

    @Override
    public int getAiMatchRatePercent(Long userId) { return 0; }

    @Override
    public int getPredictionAccuracyPercent(Long userId) { return 0; }

    @Override
    public int getFriendCount(Long userId) { return 0; }

    @Override
    public int getChatCount(Long userId) { return 0; }

    @Override
    public int getHostCount(Long userId) { return 0; }

    @Override
    public int getPersonalityMatchCount(Long userId, String personalityParam) { return 0; }

    @Override
    public int getVibeMatchCount(Long userId, String vibeParam) { return 0; }

    @Override
    public int getSeasonParticipationCount(Long userId, String seasonParam) { return 0; }

    @Override
    public int hasParticipatedOnSpecificDate(Long userId, String dateParam) { return 0; }

    @Override
    public int getAllCategoryCompleteCount(Long userId, int minEachCategoryCount) { return 0; }

    @Override
    public int isFirstLogin(Long userId) {
        return 1;
    }
}