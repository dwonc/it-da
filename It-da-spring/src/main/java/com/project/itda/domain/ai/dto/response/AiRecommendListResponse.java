package com.project.itda.domain.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * AI 모임 추천 목록 응답
 * 클라이언트에게 최종 반환
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRecommendListResponse {

    /**
     * 성공 여부
     */
    private Boolean success;

    /**
     * 메시지
     */
    private String message;

    /**
     * 사용자 ID
     */
    private Long userId;

    /**
     * 추천 모임 목록
     */
    private List<RecommendedMeetingDTO> recommendations;

    /**
     * 총 추천 개수
     */
    private Integer totalCount;

    /**
     * AI 모델 정보
     */
    private Map<String, Object> modelInfo;

    /**
     * 처리 시간 (ms)
     */
    private Long processingTimeMs;
}