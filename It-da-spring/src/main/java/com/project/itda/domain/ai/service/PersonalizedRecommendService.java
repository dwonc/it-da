package com.project.itda.domain.ai.service;

import com.project.itda.domain.ai.dto.request.PersonalizedRecommendRequest;
import com.project.itda.domain.ai.dto.response.PersonalizedRecommendResponse;
import com.project.itda.domain.meeting.entity.Meeting;
import com.project.itda.domain.meeting.repository.MeetingRepository;
import com.project.itda.domain.user.entity.User;
import com.project.itda.domain.user.entity.UserPreference;
import com.project.itda.domain.user.repository.UserPreferenceRepository;
import com.project.itda.domain.user.repository.UserRepository;
import com.project.itda.domain.review.repository.ReviewRepository;
import com.project.itda.domain.participation.repository.ParticipationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalizedRecommendService {

    private final AIServiceClient aiServiceClient;
    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final MeetingRepository meetingRepository;
    private final ReviewRepository reviewRepository;
    private final ParticipationRepository participationRepository;

    public Meeting getPersonalizedRecommendation(Long userId) {
        log.info("🎯 개인화 AI 추천 시작: userId={}", userId);

        try {
            // 1. 사용자 정보 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

            UserPreference userPref = userPreferenceRepository.findByUserId(userId)
                    .orElse(null);

            // 2. 사용자 통계 계산
            Double userAvgRating = Optional.ofNullable(reviewRepository.getAvgRatingByUserId(userId))
                    .orElse(3.5);

            Long userMeetingCount = Optional.ofNullable(
                    participationRepository.countByUserIdAndStatus(
                            userId,
                            com.project.itda.domain.participation.enums.ParticipationStatus.COMPLETED
                    )
            ).orElse(0L);

            Double userRatingStd = Optional.ofNullable(reviewRepository.getRatingStdByUserId(userId))
                    .orElse(0.5);

            // 3. 후보 모임 조회
            List<Meeting> candidateMeetings = meetingRepository
                    .findTop50ByOrderByCreatedAtDesc();

            if (candidateMeetings.isEmpty()) {
                log.warn("⚠️ 추천 가능한 모임 없음");
                return null;
            }

            // 4. FastAPI 요청 생성
            PersonalizedRecommendRequest request = PersonalizedRecommendRequest.builder()
                    .userId(userId)  // ⭐ 수정
                    .userLat(user.getLatitude() != null ? user.getLatitude() : 37.5665)
                    .userLng(user.getLongitude() != null ? user.getLongitude() : 126.9780)
                    .userInterests(userPref != null && userPref.getInterests() != null
                            ? userPref.getInterests()
                            : "[]")
                    .userTimePreference(userPref != null && userPref.getTimePreference() != null
                            ? String.valueOf(userPref.getTimePreference()).toUpperCase()
                            : "AFTERNOON")
                    .userLocationPref(userPref != null && userPref.getLocationType() != null
                            ? userPref.getLocationType().name()
                            : "INDOOR")
                    .userBudgetType(userPref != null && userPref.getBudgetType() != null
                            ? userPref.getBudgetType().name()
                            : "VALUE")
                    .userEnergyType(userPref != null && userPref.getEnergyType() != null
                            ? userPref.getEnergyType().name()
                            : "EXTROVERT")
                    .userLeadershipType(userPref != null && userPref.getLeadershipType() != null
                            ? userPref.getLeadershipType().name()
                            : "FOLLOWER")
                    .userFrequencyType(userPref != null && userPref.getFrequencyType() != null
                            ? userPref.getFrequencyType().name()
                            : "REGULAR")
                    .userPurposeType(userPref != null && userPref.getPurposeType() != null
                            ? userPref.getPurposeType().name()
                            : "TASK")
                    .userAvgRating(userAvgRating)
                    .userMeetingCount(userMeetingCount.intValue())
                    .userRatingStd(userRatingStd)
                    .candidateMeetings(candidateMeetings.stream()
                            .map(this::convertToDto)  // ⭐ 변경
                            .collect(Collectors.toList()))
                    .build();

            // 5. FastAPI 호출
            PersonalizedRecommendResponse aiResponse;
            try {
                aiResponse = aiServiceClient.post(
                        "/api/ai/recommendations/personalized-recommendation",
                        request,
                        PersonalizedRecommendResponse.class
                );
            } catch (Exception fastApiError) {
                log.warn("⚠️ FastAPI 호출 실패, fallback 사용: {}", fastApiError.getMessage());
                return candidateMeetings.get(0);
            }

            // 6. 응답 검증
            if (aiResponse == null || !Boolean.TRUE.equals(aiResponse.getSuccess())
                    || aiResponse.getRecommendation() == null) {
                log.warn("⚠️ AI 추천 실패 - 랜덤 추천");
                return candidateMeetings.get(0);
            }

            // 7. 추천된 모임 반환
            Long recommendedMeetingId = aiResponse.getMeetingId();

            if (recommendedMeetingId == null) {
                log.warn("⚠️ 추천 모임 ID 없음 - 랜덤 추천");
                return candidateMeetings.get(0);
            }

            Meeting recommended = meetingRepository.findById(recommendedMeetingId)
                    .orElse(candidateMeetings.get(0));

            log.info("✅ 개인화 추천 완료: meetingId={}, rating={}",
                    recommendedMeetingId, aiResponse.getPredictedRating());

            return recommended;

        } catch (Exception e) {
            log.error("❌ 개인화 추천 실패: {}", e.getMessage(), e);
            return meetingRepository.findTopByOrderByCreatedAtDesc()
                    .orElse(null);
        }
    }

    /**
     * Meeting → CandidateMeetingDto 변환
     */
    private PersonalizedRecommendRequest.CandidateMeetingDto convertToDto(Meeting meeting) {
        return PersonalizedRecommendRequest.CandidateMeetingDto.builder()
                .meetingId(meeting.getMeetingId())
                .latitude(meeting.getLatitude())
                .longitude(meeting.getLongitude())
                .category(meeting.getCategory())
                .subcategory(meeting.getSubcategory())
                .timeSlot(meeting.getTimeSlot().name())
                .locationType(meeting.getLocationType().name())
                .vibe(meeting.getVibe())
                .maxParticipants(meeting.getMaxParticipants())
                .expectedCost(meeting.getExpectedCost())
                .avgRating(meeting.getAvgRating())
                .ratingCount(meeting.getRatingCount())
                .currentParticipants(meeting.getCurrentParticipants())
                .build();
    }
}