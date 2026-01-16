package com.project.itda.domain.ai.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class PersonalizedRecommendResponse {

    private Boolean success;

    private String message;

    @JsonProperty("recommendation")
    private Map<String, Object> recommendation;  // ⭐ FastAPI에서 meeting_data를 그대로 받음

    @JsonProperty("predicted_rating")
    private Double predictedRating;

    @JsonProperty("total_candidates")
    private Integer totalCandidates;

    @JsonProperty("scored_count")
    private Integer scoredCount;

    // ⭐ 추천된 모임 ID 추출 메서드
    public Long getMeetingId() {
        if (recommendation == null) {
            return null;
        }
        Object meetingIdObj = recommendation.get("meetingId");
        if (meetingIdObj instanceof Number) {
            return ((Number) meetingIdObj).longValue();
        }
        return null;
    }
}