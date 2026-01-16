package com.project.itda.domain.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 매칭률 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchScoreDTO {

    private Boolean success;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("meetingId")
    private Long meetingId;

    @JsonProperty("matchPercentage")
    private Integer matchPercentage;

    @JsonProperty("matchLevel")
    private String matchLevel;  // VERY_HIGH, HIGH, MEDIUM, LOW

    @JsonProperty("rank")
    private Integer rank;  // SVD 추천 순위

    @JsonProperty("inTopRecommendations")
    private Boolean inTopRecommendations;
}