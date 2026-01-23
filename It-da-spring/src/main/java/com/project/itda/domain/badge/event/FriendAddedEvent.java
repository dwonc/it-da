// src/main/java/com/project/itda/domain/badge/event/FriendAddedEvent.java
package com.project.itda.domain.badge.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FriendAddedEvent extends ApplicationEvent {

    private final Long userId;

    public FriendAddedEvent(Long userId) {
        super(userId);
        this.userId = userId;
    }
}
