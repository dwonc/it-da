// src/main/java/com/project/itda/domain/badge/metrics/NoopBadgeMetricsProvider.java
package com.project.itda.domain.badge.metrics;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * 데이터가 아직 없는 단계에서 사용하는 기본 Provider.
 * - 모든 지표는 0을 반환
 * - 단, FIRST_LOGIN은 서버 500 방지 및 동작 확인을 위해 임시로 1을 반환
 */
@Component
@Primary
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
        // 임시: 지금은 “첫 로그인 배지” 동작 확인을 위해 항상 1로 둡니다.
        // 실제 구현 시: user.lastLoginAt, loginCount 같은 값을 보고 첫 로그인에만 1 반환하도록 변경하세요.
        return 1;
    }
}
