// src/main/java/com/project/itda/domain/badge/event/ChatSentEvent.java
package com.project.itda.domain.badge.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ChatSentEvent extends ApplicationEvent {

    private final Long userId;
    private final long totalChatCount;

    public ChatSentEvent(Long userId, long totalChatCount) {
        super(userId);
        this.userId = userId;
        this.totalChatCount = totalChatCount;
    }
}
