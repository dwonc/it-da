package com.project.itda.domain.ai.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class PersonalizedRecommendRequest {

    @JsonProperty("user_id")
    private Long userId;

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

    @JsonProperty("user_energy_type")
    private String userEnergyType;

    @JsonProperty("user_leadership_type")
    private String userLeadershipType;

    @JsonProperty("user_frequency_type")
    private String userFrequencyType;

    @JsonProperty("user_purpose_type")
    private String userPurposeType;

    @JsonProperty("user_avg_rating")
    private Double userAvgRating;

    @JsonProperty("user_meeting_count")
    private Integer userMeetingCount;

    @JsonProperty("user_rating_std")
    private Double userRatingStd;

    @JsonProperty("candidate_meetings")
    private List<CandidateMeetingDto> candidateMeetings;

    @Getter
    @Builder
    public static class CandidateMeetingDto {
        private Long meetingId;

        private Double latitude;
        private Double longitude;
        private String category;
        private String subcategory;

        @JsonProperty("time_slot")
        private String timeSlot;

        @JsonProperty("location_type")
        private String locationType;

        private String vibe;

        @JsonProperty("max_participants")
        private Integer maxParticipants;

        @JsonProperty("expected_cost")
        private Integer expectedCost;

        @JsonProperty("avg_rating")
        private Double avgRating;

        @JsonProperty("rating_count")
        private Integer ratingCount;

        @JsonProperty("current_participants")
        private Integer currentParticipants;
    }
}