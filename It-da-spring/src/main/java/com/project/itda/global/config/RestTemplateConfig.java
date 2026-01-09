package com.project.itda.global.config;

import com.project.itda.domain.ai.config.AIServiceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Autowired
    private AIServiceConfig aiServiceConfig;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(aiServiceConfig.getTimeout());
        factory.setReadTimeout(aiServiceConfig.getTimeout());

        return new RestTemplate(factory);
    }
}