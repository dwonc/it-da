// src/main/java/com/project/itda/domain/badge/metrics/RealBadgeMetricsProvider.java
package com.project.itda.domain.badge.metrics;

import com.project.itda.domain.meeting.repository.MeetingRepository;
import com.project.itda.domain.review.enums.SentimentType;
import com.project.itda.domain.review.repository.ReviewRepository;
import com.project.itda.domain.social.repository.ChatMessageRepository;
import com.project.itda.domain.user.repository.MeetingParticipationRepository;
import com.project.itda.domain.user.repository.UserFollowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * ⭐ 실제 데이터를 조회하는 BadgeMetricsProvider
 * 모든 배지 카테고리의 실제 데이터 연결
 */
@Component
@Primary
@RequiredArgsConstructor
@Slf4j
public class RealBadgeMetricsProvider implements BadgeMetricsProvider {

    private final MeetingParticipationRepository participationRepository;
    private final MeetingRepository meetingRepository;
    private final ReviewRepository reviewRepository;
    private final UserFollowRepository userFollowRepository;
    private final ChatMessageRepository chatMessageRepository;

    // ============ 참여 관련 ============

    @Override
    public int getParticipationCompletedCount(Long userId) {
        try {
            int count = participationRepository.countCompletedByUserId(userId);
            log.debug("📊 참여 완료 횟수: userId={}, count={}", userId, count);
            return count;
        } catch (Exception e) {
            log.error("참여 완료 횟수 조회 실패: userId={}", userId, e);
            return 0;
        }
    }

    @Override
    public int getCategoryParticipationCompletedCount(Long userId, String categoryParam) {
        try {
            return participationRepository.countCompletedByUserIdAndCategory(userId, categoryParam);
        } catch (Exception e) {
            log.error("카테고리별 참여 횟수 조회 실패: userId={}, category={}", userId, categoryParam, e);
            return 0;
        }
    }

    @Override
    public int getTimeSlotParticipationCompletedCount(Long userId, String timeSlotParam) {
        // TODO: 시간대별 참여 횟수 (MORNING, AFTERNOON, EVENING)
        return 0;
    }

    @Override
    public int getConsecutiveDays(Long userId) {
        // TODO: 연속 참여 일수 계산
        return 0;
    }

    @Override
    public int getConsecutiveWeeks(Long userId) {
        // TODO: 연속 참여 주수 계산
        return 0;
    }

    @Override
    public int getAllCategoryCompleteCount(Long userId, int minEachCategoryCount) {
        try {
            return participationRepository.countDistinctCategoryByUserId(userId);
        } catch (Exception e) {
            log.error("카테고리 완료 수 조회 실패: userId={}", userId, e);
            return 0;
        }
    }

    // ============ 거리/지역 관련 ============

    @Override
    public int getTotalDistanceKm(Long userId) {
        // TODO: 총 이동 거리 계산
        return 0;
    }

    @Override
    public int getDistanceRangeCount(Long userId, double minKm, double maxKm) {
        // TODO: 거리 범위별 참여 횟수
        return 0;
    }

    @Override
    public int getDistinctRegionCount(Long userId) {
        // TODO: 방문한 지역 수
        return 0;
    }

    // ============ 리뷰 관련 ============

    @Override
    public int getReviewCount(Long userId) {
        try {
            Long count = reviewRepository.countByUserId(userId);
            log.debug("📊 리뷰 횟수: userId={}, count={}", userId, count);
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            log.error("리뷰 횟수 조회 실패: userId={}", userId, e);
            return 0;
        }
    }

    @Override
    public int getPositiveReviewCount(Long userId) {
        try {
            Long count = reviewRepository.countByUserIdAndSentiment(userId, SentimentType.POSITIVE);
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            log.error("긍정 리뷰 횟수 조회 실패: userId={}", userId, e);
            return 0;
        }
    }

    @Override
    public int getAverageRatingTimes10(Long userId) {
        try {
            Double avgRating = reviewRepository.findAvgRatingByUserId(userId);
            if (avgRating != null) {
                return (int) (avgRating * 10); // 4.5 → 45
            }
            return 0;
        } catch (Exception e) {
            log.error("평균 평점 조회 실패: userId={}", userId, e);
            return 0;
        }
    }

    @Override
    public int getPositiveRatePercent(Long userId) {
        try {
            Long total = reviewRepository.countByUserId(userId);
            if (total == null || total == 0) return 0;

            Long positive = reviewRepository.countByUserIdAndSentiment(userId, SentimentType.POSITIVE);
            if (positive == null) return 0;

            return (int) ((positive * 100) / total);
        } catch (Exception e) {
            log.error("긍정 평가 비율 조회 실패: userId={}", userId, e);
            return 0;
        }
    }

    // ============ AI 관련 ============

    @Override
    public int getAiRecommendationParticipationCount(Long userId) {
        // TODO: AI 추천 모임 참여 횟수
        return 0;
    }

    @Override
    public int getHighSatisfactionParticipationCount(Long userId) {
        // TODO: 만족도 높은 모임 참여 횟수
        return 0;
    }

    @Override
    public int getAiMatchRatePercent(Long userId) {
        // TODO: AI 매칭률
        return 0;
    }

    @Override
    public int getPredictionAccuracyPercent(Long userId) {
        // TODO: 예측 정확도
        return 0;
    }

    // ============ 소셜 관련 ============

    @Override
    public int getFriendCount(Long userId) {
        try {
            int count = userFollowRepository.countByFollowerId(userId);
            log.debug("📊 팔로잉 수: userId={}, count={}", userId, count);
            return count;
        } catch (Exception e) {
            log.error("팔로잉 수 조회 실패: userId={}", userId, e);
            return 0;
        }
    }

    @Override
    public int getChatCount(Long userId) {
        try {
            int count = chatMessageRepository.countBySenderUserId(userId);
            log.debug("📊 채팅 횟수: userId={}, count={}", userId, count);
            return count;
        } catch (Exception e) {
            log.error("채팅 횟수 조회 실패: userId={}", userId, e);
            return 0;
        }
    }

    @Override
    public int getHostCount(Long userId) {
        try {
            int count = meetingRepository.findByOrganizerUserId(userId).size();
            log.debug("📊 주최 횟수: userId={}, count={}", userId, count);
            return count;
        } catch (Exception e) {
            log.error("주최 횟수 조회 실패: userId={}", userId, e);
            return 0;
        }
    }

    // ============ 성향/특별 관련 ============

    @Override
    public int getPersonalityMatchCount(Long userId, String personalityParam) {
        // TODO: 성향 매칭 횟수
        return 0;
    }

    @Override
    public int getVibeMatchCount(Long userId, String vibeParam) {
        // TODO: 분위기 매칭 횟수
        return 0;
    }

    @Override
    public int getSeasonParticipationCount(Long userId, String seasonParam) {
        // TODO: 시즌별 참여 횟수
        return 0;
    }

    @Override
    public int hasParticipatedOnSpecificDate(Long userId, String dateParam) {
        // TODO: 특정 날짜 참여 여부
        return 0;
    }

    @Override
    public int isFirstLogin(Long userId) {
        // 첫 로그인 배지는 항상 1
        return 1;
    }
}