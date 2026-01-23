// src/main/java/com/project/itda/domain/badge/service/BadgeCalculator.java
package com.project.itda.domain.badge.service;

import com.project.itda.domain.badge.entity.Badge;

/**
 * 배지 진행도 계산기(전략)
 */
public interface BadgeCalculator {

    /**
     * 특정 배지의 진행도 계산
     */
    int calculateProgress(Long userId, Badge badge);

    /**
     * 해당 계산기가 처리 가능한 배지인지 확인
     */
    boolean canHandle(Badge badge);
}
