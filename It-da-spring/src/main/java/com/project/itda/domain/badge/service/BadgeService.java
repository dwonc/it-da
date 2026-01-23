// src/main/java/com/project/itda/domain/badge/service/BadgeService.java
package com.project.itda.domain.badge.service;

import com.project.itda.domain.badge.dto.response.BadgeProgressResponse;
import com.project.itda.domain.badge.dto.response.UserBadgeResponse;
import com.project.itda.domain.badge.entity.Badge;
import com.project.itda.domain.badge.entity.UserBadge;
import com.project.itda.domain.badge.enums.BadgeConditionType;
import com.project.itda.domain.badge.event.BadgeUnlockedEvent;
import com.project.itda.domain.badge.repository.BadgeRepository;
import com.project.itda.domain.badge.repository.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final List<BadgeCalculator> calculators;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 사용자 전체 배지 조회
     */
    @Transactional(readOnly = true)
    public List<UserBadgeResponse> getUserBadges(Long userId) {
        List<UserBadge> userBadges = userBadgeRepository.findByUserId(userId);
        List<UserBadgeResponse> result = new ArrayList<>(userBadges.size());
        for (UserBadge ub : userBadges) {
            result.add(toResponse(ub));
        }
        return result;
    }

    /**
     * 사용자 획득 배지 조회
     */
    @Transactional(readOnly = true)
    public List<UserBadgeResponse> getUnlockedBadges(Long userId) {
        List<UserBadge> userBadges = userBadgeRepository.findByUserIdAndUnlocked(userId, true);
        List<UserBadgeResponse> result = new ArrayList<>(userBadges.size());
        for (UserBadge ub : userBadges) {
            result.add(toResponse(ub));
        }
        return result;
    }

    /**
     * 특정 배지 진행도 업데이트 + 조건 만족 시 자동 획득
     */
    @Transactional
    public BadgeProgressResponse updateBadgeProgress(Long userId, String badgeCode) {
        Badge badge = badgeRepository.findByBadgeCode(badgeCode)
                .orElseThrow(() -> new IllegalArgumentException("Badge not found: " + badgeCode));

        UserBadge userBadge = userBadgeRepository
                .findByUserIdAndBadge_BadgeCode(userId, badgeCode)
                .orElseGet(() -> userBadgeRepository.save(createUserBadge(userId, badge)));

        if (Boolean.TRUE.equals(userBadge.getUnlocked())) {
            return toProgressResponse(userBadge);
        }

        int currentProgress = calculateProgress(userId, badge);
        userBadge.updateProgress(currentProgress);

        if (userBadge.canUnlock()) {
            userBadge.unlock();
            eventPublisher.publishEvent(new BadgeUnlockedEvent(userId, badge));
            log.info("Badge unlocked. userId={}, badgeCode={}", userId, badgeCode);
        }

        userBadgeRepository.save(userBadge);
        return toProgressResponse(userBadge);
    }

    /**
     * 모든 배지 진행도 일괄 업데이트
     */
    @Transactional
    public List<Badge> updateAllBadgeProgress(Long userId) {
        List<Badge> allBadges = badgeRepository.findAll();
        List<Badge> newlyUnlocked = new ArrayList<>();

        for (Badge badge : allBadges) {
            UserBadge userBadge = userBadgeRepository
                    .findByUserIdAndBadge_BadgeId(userId, badge.getBadgeId())
                    .orElseGet(() -> userBadgeRepository.save(createUserBadge(userId, badge)));

            if (Boolean.TRUE.equals(userBadge.getUnlocked())) {
                continue;
            }

            int currentProgress = calculateProgress(userId, badge);
            userBadge.updateProgress(currentProgress);

            if (userBadge.canUnlock()) {
                userBadge.unlock();
                newlyUnlocked.add(badge);
                eventPublisher.publishEvent(new BadgeUnlockedEvent(userId, badge));
            }

            userBadgeRepository.save(userBadge);
        }

        return newlyUnlocked;
    }

    /**
     * 조건 타입에 해당하는 배지들만 선택 업데이트 (핵심 최적화)
     */
    @Transactional
    public List<Badge> updateBadgesByConditionType(Long userId, BadgeConditionType conditionType) {
        List<Badge> badges = badgeRepository.findByConditionType(conditionType);
        List<Badge> newlyUnlocked = new ArrayList<>();

        for (Badge badge : badges) {
            UserBadge userBadge = userBadgeRepository
                    .findByUserIdAndBadge_BadgeId(userId, badge.getBadgeId())
                    .orElseGet(() -> userBadgeRepository.save(createUserBadge(userId, badge)));

            if (Boolean.TRUE.equals(userBadge.getUnlocked())) {
                continue;
            }

            int currentProgress = calculateProgress(userId, badge);
            userBadge.updateProgress(currentProgress);

            if (userBadge.canUnlock()) {
                userBadge.unlock();
                newlyUnlocked.add(badge);
                eventPublisher.publishEvent(new BadgeUnlockedEvent(userId, badge));
            }

            userBadgeRepository.save(userBadge);
        }

        return newlyUnlocked;
    }

    private int calculateProgress(Long userId, Badge badge) {
        for (BadgeCalculator calculator : calculators) {
            if (calculator.canHandle(badge)) {
                try {
                    return calculator.calculateProgress(userId, badge);
                } catch (Exception e) {
                    log.error("Badge progress calc failed. badgeCode={}", badge.getBadgeCode(), e);
                    return 0;
                }
            }
        }
        log.warn("No calculator found. badgeCode={}, conditionType={}", badge.getBadgeCode(), badge.getConditionType());
        return 0;
    }

    private UserBadge createUserBadge(Long userId, Badge badge) {
        return UserBadge.builder()
                .userId(userId)
                .badge(badge)
                .unlocked(false)
                .progress(0)
                .build();
    }

    /**
     * ✅ 프런트(BadgeGrid)가 기대하는 "평탄화 구조"로 응답 생성
     * - badgeName/category/grade/icon 등이 최상위에 있어야 함
     */
    private UserBadgeResponse toResponse(UserBadge userBadge) {
        Badge badge = userBadge.getBadge();

        return UserBadgeResponse.builder()
                // ===== Badge 정보(최상위) =====
                .badgeId(badge.getBadgeId())
                .badgeCode(badge.getBadgeCode())
                .badgeName(badge.getBadgeName())
                .description(badge.getDescription())
                .grade(badge.getGrade() == null ? null : badge.getGrade().name())
                .category(badge.getCategory() == null ? null : badge.getCategory().name())
                .icon(badge.getIcon())

                // ===== UserBadge 정보 =====
                .unlocked(Boolean.TRUE.equals(userBadge.getUnlocked()))
                .progress(userBadge.getProgress() == null ? 0 : userBadge.getProgress())
                .targetValue(badge.getTargetValue())
                .progressPercentage(userBadge.getProgressPercentage())
                .unlockedAt(userBadge.getUnlockedAt())
                .build();
    }

    private BadgeProgressResponse toProgressResponse(UserBadge userBadge) {
        Badge b = userBadge.getBadge();
        return BadgeProgressResponse.builder()
                .badgeCode(b.getBadgeCode())
                .badgeName(b.getBadgeName())
                .progress(userBadge.getProgress())
                .targetValue(b.getTargetValue())
                .progressPercentage(userBadge.getProgressPercentage())
                .unlocked(Boolean.TRUE.equals(userBadge.getUnlocked()))
                .unlockedAt(userBadge.getUnlockedAt())
                .build();
    }
}
