package com.project.itda.domain.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 추천 모임 DTO (클라이언트에게 반환)
 * AI 점수 + DB 모임 정보 조합
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedMeetingDTO {

    // ========================================
    // 모임 기본 정보 (DB)
    // ========================================

    /**
     * 모임 ID
     */
    private Long meetingId;

    /**
     * 모임 제목
     */
    private String title;

    /**
     * 모임 설명
     */
    private String description;

    /**
     * 카테고리
     */
    private String category;

    /**
     * 서브카테고리
     */
    private String subcategory;

    /**
     * 모임 일시
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime meetingTime;

    /**
     * 장소명
     */
    private String locationName;

    /**
     * 장소 주소
     */
    private String locationAddress;

    /**
     * 위도
     */
    private Double latitude;

    /**
     * 경도
     */
    private Double longitude;

    /**
     * 분위기
     */
    private String vibe;

    /**
     * 현재 참가자 수
     */
    private Integer currentParticipants;

    /**
     * 최대 참가자 수
     */
    private Integer maxParticipants;

    /**
     * 예상 비용
     */
    private Integer expectedCost;

    /**
     * 대표 이미지 URL
     */
    private String imageUrl;

    /**
     * 모임 상태
     */
    private String status;

    // ========================================
    // AI 추천 정보
    // ========================================

    /**
     * AI 추천 점수 (1~5)
     */
    private Double aiScore;

    /**
     * 추천 순위
     */
    private Integer rank;

    /**
     * 거리 (km)
     */
    private Double distanceKm;

    /**
     * 추천 이유
     */
    private String recommendReason;

    // ========================================
    // 주최자 정보
    // ========================================

    /**
     * 주최자 ID
     */
    private Long organizerId;

    /**
     * 주최자 닉네임
     */
    private String organizerUsername;

    /**
     * 주최자 프로필 이미지
     */
    private String organizerProfileImage;
}