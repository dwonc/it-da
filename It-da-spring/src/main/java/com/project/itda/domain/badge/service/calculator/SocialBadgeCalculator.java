// src/main/java/com/project/itda/domain/badge/service/calculator/SocialBadgeCalculator.java
package com.project.itda.domain.badge.service.calculator;

import com.project.itda.domain.badge.entity.Badge;
import com.project.itda.domain.badge.enums.BadgeConditionType;
import com.project.itda.domain.badge.metrics.BadgeMetricsProvider;
import com.project.itda.domain.badge.service.BadgeCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocialBadgeCalculator implements BadgeCalculator {

    private final BadgeMetricsProvider metricsProvider;

    @Override
    public int calculateProgress(Long userId, Badge badge) {
        BadgeConditionType type = badge.getConditionType();
        String param = badge.getConditionParam();

        return switch (type) {
            case FRIEND_COUNT -> metricsProvider.getFriendCount(userId);
            case CHAT_COUNT -> metricsProvider.getChatCount(userId);
            case HOST_COUNT -> metricsProvider.getHostCount(userId);
            case PERSONALITY_MATCH_COUNT -> metricsProvider.getPersonalityMatchCount(userId, param);
            case VIBE_MATCH_COUNT -> metricsProvider.getVibeMatchCount(userId, param);
            case SEASON_PARTICIPATION -> metricsProvider.getSeasonParticipationCount(userId, param);
            case SPECIFIC_DATE -> metricsProvider.hasParticipatedOnSpecificDate(userId, param);
            default -> 0;
        };
    }

    @Override
    public boolean canHandle(Badge badge) {
        return switch (badge.getConditionType()) {
            case FRIEND_COUNT, CHAT_COUNT, HOST_COUNT,
                 PERSONALITY_MATCH_COUNT, VIBE_MATCH_COUNT,
                 SEASON_PARTICIPATION, SPECIFIC_DATE -> true;
            default -> false;
        };
    }
}
