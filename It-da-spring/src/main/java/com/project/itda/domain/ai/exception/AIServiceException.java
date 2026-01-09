package com.project.itda.domain.ai.exception;

import lombok.Getter;

/**
 * AI 서비스 호출 실패 시 발생하는 예외
 */
@Getter
public class AIServiceException extends RuntimeException {

    private final String errorCode;
    private final String aiService;

    /**
     * 기본 생성자
     */
    public AIServiceException(String message) {
        super(message);
        this.errorCode = "AI_SERVICE_ERROR";
        this.aiService = "FastAPI";
    }

    /**
     * 원인 예외 포함 생성자
     */
    public AIServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "AI_SERVICE_ERROR";
        this.aiService = "FastAPI";
    }

    /**
     * 상세 정보 포함 생성자
     */
    public AIServiceException(String message, String errorCode, String aiService) {
        super(message);
        this.errorCode = errorCode;
        this.aiService = aiService;
    }

    /**
     * 상세 정보 + 원인 예외 포함 생성자
     */
    public AIServiceException(String message, String errorCode, String aiService, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.aiService = aiService;
    }
}