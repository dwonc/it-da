package com.project.itda.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 카카오맵 API 설정
 */
@Component
@ConfigurationProperties(prefix = "kakao")
@Getter
@Setter
public class KakaoMapConfig {

    /**
     * 카카오 REST API Key
     */
    private String restApiKey;

    /**
     * 카카오 로컬 API URL
     */
    private String localApiUrl;

    /**
     * 카카오맵 검색 설정
     */
    private MapSearch map = new MapSearch();

    @Getter
    @Setter
    public static class MapSearch {
        private Search search = new Search();
    }

    @Getter
    @Setter
    public static class Search {
        private Radius radius = new Radius();
        private int size = 15;
    }

    @Getter
    @Setter
    public static class Radius {
        private int defaultValue = 3000;
        private int max = 5000;
    }
}