// src/main/java/com/project/itda/domain/badge/event/ParticipationCompletedEvent.java
package com.project.itda.domain.badge.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ParticipationCompletedEvent extends ApplicationEvent {

    private final Long userId;

    public ParticipationCompletedEvent(Long userId) {
        super(userId);
        this.userId = userId;
    }
}
