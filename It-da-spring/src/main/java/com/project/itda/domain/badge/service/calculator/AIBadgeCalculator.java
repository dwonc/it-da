// src/main/java/com/project/itda/domain/badge/service/calculator/AIBadgeCalculator.java
package com.project.itda.domain.badge.service.calculator;

import com.project.itda.domain.badge.entity.Badge;
import com.project.itda.domain.badge.enums.BadgeConditionType;
import com.project.itda.domain.badge.metrics.BadgeMetricsProvider;
import com.project.itda.domain.badge.service.BadgeCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AIBadgeCalculator implements BadgeCalculator {

    private final BadgeMetricsProvider metricsProvider;

    @Override
    public int calculateProgress(Long userId, Badge badge) {
        BadgeConditionType type = badge.getConditionType();

        return switch (type) {
            case AI_RECOMMENDATION_COUNT -> metricsProvider.getAiRecommendationParticipationCount(userId);
            case HIGH_SATISFACTION_COUNT -> metricsProvider.getHighSatisfactionParticipationCount(userId);
            case AI_MATCH_RATE -> metricsProvider.getAiMatchRatePercent(userId);
            case PREDICTION_ACCURACY -> metricsProvider.getPredictionAccuracyPercent(userId);
            default -> 0;
        };
    }

    @Override
    public boolean canHandle(Badge badge) {
        return switch (badge.getConditionType()) {
            case AI_RECOMMENDATION_COUNT, HIGH_SATISFACTION_COUNT, AI_MATCH_RATE, PREDICTION_ACCURACY -> true;
            default -> false;
        };
    }
}
