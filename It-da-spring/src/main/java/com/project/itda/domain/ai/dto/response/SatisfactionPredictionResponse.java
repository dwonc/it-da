package com.project.itda.domain.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SatisfactionPredictionResponse {

    // ✅ 이 필드들이 FastAPI 응답과 일치해야 함
    private Boolean success;
    private String message;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("meetingId")
    private Long meetingId;

    @JsonProperty("predictedRating")
    private Double predictedRating;

    @JsonProperty("ratingStars")
    private String ratingStars;

    @JsonProperty("satisfactionLevel")
    private String satisfactionLevel;

    @JsonProperty("recommended")
    private Boolean recommended;

    @JsonProperty("reasons")
    private List<ReasonItem> reasons;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReasonItem {
        private String icon;
        private String text;
    }
}
