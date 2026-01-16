package com.project.itda.domain.ai.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SatisfactionPredictionRequest {

    // ✅ @JsonProperty로 snake_case 변환
    @JsonProperty("user_id")
    private Integer userId;

    @JsonProperty("meeting_id")
    private Integer meetingId;

    // 사용자 피처
    @JsonProperty("user_lat")
    private Double userLat;

    @JsonProperty("user_lng")
    private Double userLng;

    @JsonProperty("user_interests")
    private String userInterests;

    @JsonProperty("user_time_preference")
    private String userTimePreference;

    @JsonProperty("user_location_pref")
    private String userLocationPref;

    @JsonProperty("user_budget_type")
    private String userBudgetType;

    @JsonProperty("user_avg_rating")
    private Double userAvgRating;

    @JsonProperty("user_meeting_count")
    private Integer userMeetingCount;

    @JsonProperty("user_rating_std")
    private Double userRatingStd;

    // 모임 피처
    @JsonProperty("meeting_lat")
    private Double meetingLat;

    @JsonProperty("meeting_lng")
    private Double meetingLng;

    @JsonProperty("meeting_category")
    private String meetingCategory;

    @JsonProperty("meeting_subcategory")
    private String meetingSubcategory;

    @JsonProperty("meeting_time_slot")
    private String meetingTimeSlot;

    @JsonProperty("meeting_location_type")
    private String meetingLocationType;

    @JsonProperty("meeting_vibe")
    private String meetingVibe;

    @JsonProperty("meeting_max_participants")
    private Integer meetingMaxParticipants;

    @JsonProperty("meeting_expected_cost")
    private Double meetingExpectedCost;

    @JsonProperty("meeting_avg_rating")
    private Double meetingAvgRating;

    @JsonProperty("meeting_rating_count")
    private Integer meetingRatingCount;

    @JsonProperty("meeting_participant_count")
    private Integer meetingParticipantCount;
}