// src/main/java/com/project/itda/domain/badge/dto/response/UserBadgeResponse.java
package com.project.itda.domain.badge.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserBadgeResponse {

    // ===== Badge 정보(최상위) =====
    private Long badgeId;
    private String badgeCode;
    private String badgeName;
    private String description;
    private String grade;     // "COMMON" / "RARE" ...
    private String category;  // "PARTICIPATION" / "REVIEW" ...
    private String icon;

    // ===== UserBadge 정보 =====
    private boolean unlocked;
    private int progress;
    private Integer targetValue;
    private double progressPercentage;
    private LocalDateTime unlockedAt;
}
