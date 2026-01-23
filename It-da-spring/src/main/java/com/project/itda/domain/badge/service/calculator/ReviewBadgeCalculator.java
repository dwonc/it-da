// src/main/java/com/project/itda/domain/badge/service/calculator/ReviewBadgeCalculator.java
package com.project.itda.domain.badge.service.calculator;

import com.project.itda.domain.badge.entity.Badge;
import com.project.itda.domain.badge.enums.BadgeConditionType;
import com.project.itda.domain.badge.metrics.BadgeMetricsProvider;
import com.project.itda.domain.badge.service.BadgeCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewBadgeCalculator implements BadgeCalculator {

    private final BadgeMetricsProvider metricsProvider;

    @Override
    public int calculateProgress(Long userId, Badge badge) {
        BadgeConditionType type = badge.getConditionType();

        return switch (type) {
            case REVIEW_COUNT -> metricsProvider.getReviewCount(userId);
            case POSITIVE_REVIEW_COUNT -> metricsProvider.getPositiveReviewCount(userId);
            case AVERAGE_RATING -> metricsProvider.getAverageRatingTimes10(userId);
            case POSITIVE_RATE -> metricsProvider.getPositiveRatePercent(userId);
            default -> 0;
        };
    }

    @Override
    public boolean canHandle(Badge badge) {
        return switch (badge.getConditionType()) {
            case REVIEW_COUNT, POSITIVE_REVIEW_COUNT, AVERAGE_RATING, POSITIVE_RATE -> true;
            default -> false;
        };
    }
}
