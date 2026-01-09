package com.project.itda.domain.ai.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * SVD 모임 추천 요청 (FastAPI 호출용)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingRecommendRequest {

    /**
     * 사용자 ID
     */
    @NotNull(message = "사용자 ID는 필수입니다")
    private Integer userId;

    /**
     * 추천 개수
     * 기본값: 10
     * 범위: 1~50
     */
    @Min(value = 1, message = "추천 개수는 최소 1개 이상이어야 합니다")
    @Max(value = 50, message = "추천 개수는 최대 50개까지 가능합니다")
    @Builder.Default
    private Integer topN = 10;
}