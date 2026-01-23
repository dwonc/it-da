package com.project.itda.domain.badge.dto;

import com.project.itda.domain.badge.enums.BadgeCategory;
import com.project.itda.domain.badge.enums.BadgeGrade;
import lombok.Builder;

/**
 * 배지 메타(정의) DTO
 * - UserBadgeResponse에서 badge 필드로 재사용
 */
@Builder
public record BadgeResponse(
        Long badgeId,
        String badgeCode,
        String badgeName,
        String description,
        BadgeGrade grade,
        BadgeCategory category,
        String icon
) {
}
