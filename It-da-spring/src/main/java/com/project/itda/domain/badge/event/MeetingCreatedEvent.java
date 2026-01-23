// src/main/java/com/project/itda/domain/badge/event/MeetingCreatedEvent.java
package com.project.itda.domain.badge.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class MeetingCreatedEvent extends ApplicationEvent {

    private final Long organizerId;

    public MeetingCreatedEvent(Long organizerId) {
        super(organizerId);
        this.organizerId = organizerId;
    }
}
