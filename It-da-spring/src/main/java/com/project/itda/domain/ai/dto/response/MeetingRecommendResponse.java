package com.project.itda.domain.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * SVD 모임 추천 응답 (FastAPI에서 받음)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MeetingRecommendResponse {

    /**
     * 성공 여부
     */
    private Boolean success;

    /**
     * 사용자 ID
     */
    @JsonProperty("user_id")
    private Integer userId;

    /**
     * 추천 목록
     */
    private List<RecommendedMeeting> recommendations;

    /**
     * 모델 정보
     */
    @JsonProperty("model_info")
    private ModelInfo modelInfo;

    /**
     * 추천 모임 정보
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedMeeting {

        /**
         * 모임 ID
         */
        @JsonProperty("meeting_id")
        private Integer meetingId;

        /**
         * AI 추천 점수
         */
        private Double score;

        /**
         * 순위
         */
        private Integer rank;
    }

    /**
     * SVD 모델 정보
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModelInfo {

        /**
         * RMSE (Root Mean Square Error)
         */
        private Double rmse;

        /**
         * MAE (Mean Absolute Error)
         */
        private Double mae;

        /**
         * 정확도
         */
        private Double accuracy;
    }
}