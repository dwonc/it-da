// src/main/java/com/project/itda/domain/badge/enums/BadgeGrade.java
package com.project.itda.domain.badge.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 배지 등급
 */
@Getter
@RequiredArgsConstructor
public enum BadgeGrade {
    COMMON("일반", 0),
    RARE("레어", 1),
    EPIC("에픽", 2),
    LEGENDARY("전설", 3);

    private final String displayName;
    private final int level;
}
