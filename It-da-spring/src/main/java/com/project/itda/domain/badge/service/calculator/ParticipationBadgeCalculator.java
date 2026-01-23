// src/main/java/com/project/itda/domain/badge/service/calculator/ParticipationBadgeCalculator.java
package com.project.itda.domain.badge.service.calculator;

import com.project.itda.domain.badge.entity.Badge;
import com.project.itda.domain.badge.enums.BadgeConditionType;
import com.project.itda.domain.badge.metrics.BadgeMetricsProvider;
import com.project.itda.domain.badge.service.BadgeCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParticipationBadgeCalculator implements BadgeCalculator {

    private final BadgeMetricsProvider metricsProvider;

    @Override
    public int calculateProgress(Long userId, Badge badge) {
        BadgeConditionType type = badge.getConditionType();
        String param = badge.getConditionParam();

        return switch (type) {
            case PARTICIPATION_COUNT -> metricsProvider.getParticipationCompletedCount(userId);
            case CATEGORY_COUNT -> metricsProvider.getCategoryParticipationCompletedCount(userId, param);
            case TIME_SLOT_COUNT -> metricsProvider.getTimeSlotParticipationCompletedCount(userId, param);
            case CONSECUTIVE_DAYS -> metricsProvider.getConsecutiveDays(userId);
            case CONSECUTIVE_WEEKS -> metricsProvider.getConsecutiveWeeks(userId);
            case ALL_CATEGORY_COMPLETE -> metricsProvider.getAllCategoryCompleteCount(userId, badge.getTargetValue());
            default -> 0;
        };
    }

    @Override
    public boolean canHandle(Badge badge) {
        return switch (badge.getConditionType()) {
            case PARTICIPATION_COUNT, CATEGORY_COUNT, TIME_SLOT_COUNT, CONSECUTIVE_DAYS, CONSECUTIVE_WEEKS, ALL_CATEGORY_COMPLETE -> true;
            default -> false;
        };
    }
}
