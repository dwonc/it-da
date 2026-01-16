package com.project.itda.domain.ai.service;

import com.project.itda.domain.ai.config.AIServiceConfig;
import com.project.itda.domain.ai.dto.request.MatchScoresRequestDto;
import com.project.itda.domain.ai.dto.response.MatchScoreDTO;
import com.project.itda.domain.ai.dto.response.MatchScoresResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class MatchScoreService {

    private final AIServiceClient aiServiceClient;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AIServiceConfig config;

    /**
     * AI 매칭률 조회
     */
    @Cacheable(
            cacheNames = "matchScores",
            key = "@cacheKeyUtil.matchKey(#userId, #meetingIds)"
    )
    public MatchScoreDTO getMatchScore(Long userId, Long meetingId) {
        Map<String, Object> request = Map.of(
                "user_id", userId,
                "meeting_id", meetingId
        );

        return aiServiceClient.get("/api/ai/recommendations/match-score",
                request,
                MatchScoreDTO.class);
    }

    @Cacheable(
            cacheNames = "matchScores",
            key = "@cacheKeyUtil.matchKey(#userId, #meetingIds)"
    )
    public MatchScoresResponse getMatchScores(Long userId, List<Long> meetingIds) {
        if (userId == null) throw new IllegalArgumentException("userId is required");
        if (meetingIds == null) meetingIds = List.of();

        Map<String, Object> request = new HashMap<>();
        request.put("user_id", userId);
        request.put("meeting_ids", meetingIds);

        return aiServiceClient.post(
                "/api/ai/recommendations/match-scores",
                request,
                MatchScoresResponse.class
        );
    }
}