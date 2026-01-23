package com.project.itda.domain.badge.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 배지 진행도 업데이트 결과 응답 DTO
 * - 특정 배지 업데이트 요청(수동/자동 트리거) 결과를 가볍게 반환
 */
@Builder
public record BadgeProgressResponse(
        String badgeCode,
        String badgeName,
        int progress,
        int targetValue,
        double progressPercentage,
        boolean unlocked,
        LocalDateTime unlockedAt
) {
}
