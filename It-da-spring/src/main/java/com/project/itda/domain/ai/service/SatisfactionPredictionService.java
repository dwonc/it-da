package com.project.itda.domain.ai.service;

import com.project.itda.domain.ai.dto.request.SatisfactionPredictionRequest;
import com.project.itda.domain.ai.dto.response.SatisfactionPredictionDTO;
import com.project.itda.domain.ai.dto.response.SatisfactionPredictionResponse;
import com.project.itda.domain.meeting.entity.Meeting;
import com.project.itda.domain.meeting.repository.MeetingRepository;
import com.project.itda.domain.participation.repository.ParticipationRepository;
import com.project.itda.domain.review.repository.ReviewRepository;
import com.project.itda.domain.user.entity.User;
import com.project.itda.domain.user.entity.UserPreference;
import com.project.itda.domain.user.repository.UserPreferenceRepository;
import com.project.itda.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 기반 만족도 예측 서비스
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SatisfactionPredictionService {

    private final AIServiceClient aiServiceClient;
    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final MeetingRepository meetingRepository;
    private final ReviewRepository reviewRepository;
    private final ParticipationRepository participationRepository;

    /**
     * 모임 상세 페이지 만족도 예측
     *
     * @param userId 사용자 ID
     * @param meetingId 모임 ID
     * @return 예측 만족도
     */
    public SatisfactionPredictionDTO predictSatisfaction(Long userId, Long meetingId) {
        log.info("🎯 만족도 예측 시작 - userId: {}, meetingId: {}", userId, meetingId);

        try {
            // 1. 사용자 정보 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

            // UserPreference 조회 (없으면 null, 나중에 기본값 사용)
            UserPreference userPref = userPreferenceRepository.findByUserId(userId)
                    .orElse(null);

            // 2. 모임 정보 조회
            Meeting meeting = meetingRepository.findById(meetingId)
                    .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없습니다: " + meetingId));

            // 3. 사용자 통계 계산
            Double userAvgRating = reviewRepository.getAvgRatingByUserId(userId);
            if (userAvgRating == null || userAvgRating == 0.0) {
                userAvgRating = 3.5; // 기본값
            }

            Long userMeetingCount = participationRepository.countByUserIdAndStatus(
                    userId,
                    com.project.itda.domain.participation.enums.ParticipationStatus.COMPLETED
            );
            if (userMeetingCount == null) {
                userMeetingCount = 0L;
            }

            Double userRatingStd = reviewRepository.getRatingStdByUserId(userId);
            if (userRatingStd == null || userRatingStd == 0.0) {
                userRatingStd = 0.5; // 기본값
            }

            // 4. 모임 통계 계산
            Double meetingAvgRating = reviewRepository.getAvgRatingByMeetingId(meetingId);
            if (meetingAvgRating == null || meetingAvgRating == 0.0) {
                meetingAvgRating = 4.0; // 기본값
            }

            Long meetingRatingCount = reviewRepository.countByMeetingId(meetingId);
            if (meetingRatingCount == null) {
                meetingRatingCount = 0L;
            }

            // 5. 안전한 값 추출
            Double userLat = user.getLatitude() != null ? user.getLatitude() : 37.5665;
            Double userLng = user.getLongitude() != null ? user.getLongitude() : 126.9780;
            Double meetingLat = meeting.getLatitude() != null ? meeting.getLatitude() : 37.5665;
            Double meetingLng = meeting.getLongitude() != null ? meeting.getLongitude() : 126.9780;

            String userInterests = userPref.getInterests() != null ? userPref.getInterests() : "";
            String userTimePreference = userPref.getTimePreference() != null
                    ? userPref.getTimePreference().toLowerCase()
                    : "afternoon";
            String userLocationPref = userPref.getLocationType() != null
                    ? userPref.getLocationType().toString().toLowerCase()
                    : "indoor";
            String userBudgetType = userPref.getBudgetType() != null
                    ? userPref.getBudgetType().toString().toLowerCase()
                    : "value";

            String meetingCategory = meeting.getCategory() != null ? meeting.getCategory() : "스포츠";
            String meetingSubcategory = meeting.getSubcategory() != null ? meeting.getSubcategory() : "";
            String meetingTimeSlot = meeting.getTimeSlot() != null
                    ? meeting.getTimeSlot().name().toLowerCase()
                    : "afternoon";
            String meetingLocationType = meeting.getLocationType() != null
                    ? meeting.getLocationType().name().toLowerCase()
                    : "indoor";
            String meetingVibe = meeting.getVibe() != null ? meeting.getVibe() : "여유로운";

            // 6. FastAPI 요청 생성
            SatisfactionPredictionRequest request = SatisfactionPredictionRequest.builder()
                    // 기본 ID
                    .userId(userId.intValue())
                    .meetingId(meetingId.intValue())
                    // 사용자 피처
                    .userLat(userLat)
                    .userLng(userLng)
                    .userInterests(userInterests)
                    .userTimePreference(userTimePreference)
                    .userLocationPref(userLocationPref)
                    .userBudgetType(userBudgetType)
                    .userAvgRating(userAvgRating)
                    .userMeetingCount(userMeetingCount.intValue())
                    .userRatingStd(userRatingStd)
                    // 모임 피처
                    .meetingLat(meetingLat)
                    .meetingLng(meetingLng)
                    .meetingCategory(meetingCategory)
                    .meetingSubcategory(meetingSubcategory)
                    .meetingTimeSlot(meetingTimeSlot)
                    .meetingLocationType(meetingLocationType)
                    .meetingVibe(meetingVibe)
                    .meetingMaxParticipants(meeting.getMaxParticipants())
                    .meetingExpectedCost(meeting.getExpectedCost() != null ? meeting.getExpectedCost().doubleValue() : 0.0)
                    .meetingAvgRating(meetingAvgRating)
                    .meetingRatingCount(meetingRatingCount.intValue())
                    .meetingParticipantCount(meeting.getCurrentParticipants())
                    .build();

            log.info("📤 FastAPI 요청: {}", request);

            // 7. ✅ FastAPI POST 호출
            SatisfactionPredictionResponse aiResponse = aiServiceClient.predictSatisfaction(request);

            log.info("📥 FastAPI 응답: success={}, predictedRating={}, reasons={}",
                    aiResponse.getSuccess(),
                    aiResponse.getPredictedRating(),
                    aiResponse.getReasons() != null ? aiResponse.getReasons().size() : 0);

            if (aiResponse == null || !Boolean.TRUE.equals(aiResponse.getSuccess())) {
                log.warn("⚠️ AI 만족도 예측 실패 - userId: {}, meetingId: {}", userId, meetingId);
                return buildErrorResponse(userId, meetingId);
            }

            // 8. 거리 계산
            Double distanceKm = calculateDistance(userLat, userLng, meetingLat, meetingLng);

            // 9. 응답 생성
            Double predictedRating = aiResponse.getPredictedRating();
            String ratingStars = generateRatingStars(predictedRating);
            String satisfactionLevel = getSatisfactionLevel(predictedRating);
            boolean recommended = predictedRating != null && predictedRating >= 4.0;

            List<SatisfactionPredictionDTO.ReasonItem> reasons = List.of();
            if (aiResponse.getReasons() != null && !aiResponse.getReasons().isEmpty()) {
                reasons = aiResponse.getReasons().stream()
                        .map(r -> SatisfactionPredictionDTO.ReasonItem.builder()
                                .icon(r.getIcon())
                                .text(r.getText())
                                .build())
                        .collect(Collectors.toList());
            }

            log.info("✅ 만족도 예측 완료 - predictedRating: {}, recommended: {}, reasons: {}",
                    predictedRating, recommended, reasons.size());

            return SatisfactionPredictionDTO.builder()
                    .success(true)
                    .message("만족도 예측 성공")
                    .userId(userId)
                    .meetingId(meetingId)
                    .predictedRating(predictedRating)
                    .ratingStars(ratingStars)
                    .satisfactionLevel(satisfactionLevel)
                    .recommended(recommended)
                    .reasons(reasons)
                    .distanceKm(distanceKm)
                    .build();

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ 입력 오류: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ 만족도 예측 실패: {}", e.getMessage(), e);
            return buildErrorResponse(userId, meetingId);
        }
    }

    /**
     * 기본 UserPreference 생성
     */
    private UserPreference createDefaultUserPreference(Long userId) {
        log.warn("⚠️ UserPreference 없음, 기본값 사용 - userId: {}", userId);

        // UserPreference가 Builder 패턴을 사용하는 경우
        return UserPreference.builder()
                .interests("")
                .timePreference("afternoon")
                .build();

        // 또는 생성자가 있는 경우
        // return new UserPreference(userId, "", "afternoon", ...);
    }

    /**
     * 에러 응답 생성
     */
    private SatisfactionPredictionDTO buildErrorResponse(Long userId, Long meetingId) {
        return SatisfactionPredictionDTO.builder()
                .success(false)
                .message("만족도 예측 실패")
                .userId(userId)
                .meetingId(meetingId)
                .predictedRating(null)
                .ratingStars(null)
                .satisfactionLevel(null)
                .recommended(false)
                .reasons(List.of())
                .distanceKm(null)
                .build();
    }

    /**
     * 별점 문자열 생성
     */
    private String generateRatingStars(Double rating) {
        if (rating == null) return "";

        int fullStars = (int) Math.floor(rating);
        boolean hasHalfStar = (rating - fullStars) >= 0.5;

        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < fullStars; i++) {
            stars.append("⭐");
        }
        if (hasHalfStar && fullStars < 5) {
            stars.append("⭐");
        }

        return stars.toString();
    }

    /**
     * 만족도 수준 텍스트
     */
    private String getSatisfactionLevel(Double rating) {
        if (rating == null) return "알 수 없음";
        if (rating >= 4.5) return "매우 높음";
        if (rating >= 4.0) return "높음";
        if (rating >= 3.5) return "보통";
        if (rating >= 3.0) return "낮음";
        return "매우 낮음";
    }

    /**
     * 거리 계산 (Haversine)
     */
    private Double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return null;
        }

        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}