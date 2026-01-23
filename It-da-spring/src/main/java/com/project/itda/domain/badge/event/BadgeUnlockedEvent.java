// src/main/java/com/project/itda/domain/badge/event/BadgeUnlockedEvent.java
package com.project.itda.domain.badge.event;

import com.project.itda.domain.badge.entity.Badge;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class BadgeUnlockedEvent extends ApplicationEvent {

    private final Long userId;
    private final Badge badge;

    public BadgeUnlockedEvent(Long userId, Badge badge) {
        super(userId);
        this.userId = userId;
        this.badge = badge;
    }
}
