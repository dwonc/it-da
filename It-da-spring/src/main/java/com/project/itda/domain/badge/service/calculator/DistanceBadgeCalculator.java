// src/main/java/com/project/itda/domain/badge/service/calculator/DistanceBadgeCalculator.java
package com.project.itda.domain.badge.service.calculator;

import com.project.itda.domain.badge.entity.Badge;
import com.project.itda.domain.badge.enums.BadgeConditionType;
import com.project.itda.domain.badge.metrics.BadgeMetricsProvider;
import com.project.itda.domain.badge.service.BadgeCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DistanceBadgeCalculator implements BadgeCalculator {

    private final BadgeMetricsProvider metricsProvider;

    @Override
    public int calculateProgress(Long userId, Badge badge) {
        BadgeConditionType type = badge.getConditionType();
        String param = badge.getConditionParam();

        return switch (type) {
            case TOTAL_DISTANCE -> metricsProvider.getTotalDistanceKm(userId);
            case REGION_COUNT -> metricsProvider.getDistinctRegionCount(userId);
            case DISTANCE_RANGE_COUNT -> {
                // param 예: "0-3"
                if (param == null || !param.contains("-")) {
                    yield 0;
                }
                String[] parts = param.split("-");
                double min = Double.parseDouble(parts[0]);
                double max = Double.parseDouble(parts[1]);
                yield metricsProvider.getDistanceRangeCount(userId, min, max);
            }
            default -> 0;
        };
    }

    @Override
    public boolean canHandle(Badge badge) {
        return switch (badge.getConditionType()) {
            case TOTAL_DISTANCE, DISTANCE_RANGE_COUNT, REGION_COUNT -> true;
            default -> false;
        };
    }
}
