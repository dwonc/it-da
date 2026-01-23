// src/main/java/com/project/itda/domain/badge/listener/BadgeEventListener.java
package com.project.itda.domain.badge.listener;

import com.project.itda.domain.badge.enums.BadgeConditionType;
import com.project.itda.domain.badge.event.*;
import com.project.itda.domain.badge.notification.BadgeNotificationPort;
import com.project.itda.domain.badge.service.BadgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class BadgeEventListener {

    private final BadgeService badgeService;
    private final BadgeNotificationPort badgeNotificationPort;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleParticipationCompleted(ParticipationCompletedEvent event) {
        Long userId = event.getUserId();

        badgeService.updateBadgesByConditionType(userId, BadgeConditionType.PARTICIPATION_COUNT);
        badgeService.updateBadgesByConditionType(userId, BadgeConditionType.CATEGORY_COUNT);
        badgeService.updateBadgesByConditionType(userId, BadgeConditionType.TIME_SLOT_COUNT);
        badgeService.updateBadgesByConditionType(userId, BadgeConditionType.CONSECUTIVE_DAYS);
        badgeService.updateBadgesByConditionType(userId, BadgeConditionType.CONSECUTIVE_WEEKS);
        badgeService.updateBadgesByConditionType(userId, BadgeConditionType.TOTAL_DISTANCE);
        badgeService.updateBadgesByConditionType(userId, BadgeConditionType.DISTANCE_RANGE_COUNT);
        badgeService.updateBadgesByConditionType(userId, BadgeConditionType.REGION_COUNT);
        badgeService.updateBadgesByConditionType(userId, BadgeConditionType.ALL_CATEGORY_COMPLETE);

        log.info("Participation event handled. userId={}", userId);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReviewCreated(ReviewCreatedEvent event) {
        Long userId = event.getUserId();

        badgeService.updateBadgesByConditionType(userId, BadgeConditionType.REVIEW_COUNT);
        badgeService.updateBadgesByConditionType(userId, BadgeConditionType.AVERAGE_RATING);
        badgeService.updateBadgesByConditionType(userId, BadgeConditionType.POSITIVE_RATE);

        if ("positive".equalsIgnoreCase(event.getSentiment())) {
            badgeService.updateBadgesByConditionType(userId, BadgeConditionType.POSITIVE_REVIEW_COUNT);
        }

        log.info("Review event handled. userId={}", userId);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFriendAdded(FriendAddedEvent event) {
        Long userId = event.getUserId();
        badgeService.updateBadgesByConditionType(userId, BadgeConditionType.FRIEND_COUNT);
        log.info("Friend event handled. userId={}", userId);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChatSent(ChatSentEvent event) {
        Long userId = event.getUserId();

        // 채팅은 빈번하므로 10회 단위로만 체크
        if (event.getTotalChatCount() % 10 == 0) {
            badgeService.updateBadgesByConditionType(userId, BadgeConditionType.CHAT_COUNT);
            log.info("Chat event handled. userId={}, total={}", userId, event.getTotalChatCount());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMeetingCreated(MeetingCreatedEvent event) {
        Long userId = event.getOrganizerId();
        badgeService.updateBadgesByConditionType(userId, BadgeConditionType.HOST_COUNT);
        log.info("MeetingCreated event handled. organizerId={}", userId);
    }

    @EventListener
    public void handleBadgeUnlocked(BadgeUnlockedEvent event) {
        badgeNotificationPort.sendBadgeUnlocked(event.getUserId(), event.getBadge());
        log.info("BadgeUnlocked event handled. userId={}, badgeCode={}", event.getUserId(), event.getBadge().getBadgeCode());
    }
}
