// src/main/java/com/project/itda/domain/badge/event/ReviewCreatedEvent.java
package com.project.itda.domain.badge.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ReviewCreatedEvent extends ApplicationEvent {

    private final Long userId;
    private final String sentiment; // 예: "positive", "negative"

    public ReviewCreatedEvent(Long userId, String sentiment) {
        super(userId);
        this.userId = userId;
        this.sentiment = sentiment;
    }
}
