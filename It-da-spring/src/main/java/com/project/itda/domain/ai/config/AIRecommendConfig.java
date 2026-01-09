package com.project.itda.domain.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 추천 설정
 */
@Component
@ConfigurationProperties(prefix = "ai.recommend")
@Getter
@Setter
public class AIRecommendConfig {

    /**
     * SVD 협업 필터링 설정
     */
    private Svd svd = new Svd();

    /**
     * 만족도 예측 설정
     */
    private Satisfaction satisfaction = new Satisfaction();

    @Getter
    @Setter
    public static class Svd {
        /**
         * 추천 개수
         */
        private int topN = 10;

        /**
         * 최소 점수
         */
        private double minScore = 3.0;
    }

    @Getter
    @Setter
    public static class Satisfaction {
        /**
         * 만족 임계값
         */
        private double threshold = 4.0;
    }
}