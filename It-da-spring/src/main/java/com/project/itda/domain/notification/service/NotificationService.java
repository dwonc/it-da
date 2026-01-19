package com.project.itda.domain.notification.service;

import com.project.itda.domain.notification.dto.request.NotificationCreateRequest;
import com.project.itda.domain.notification.dto.response.NotificationListResponse;
import com.project.itda.domain.notification.dto.response.NotificationResponse;
import com.project.itda.domain.notification.entity.Notification;
import com.project.itda.domain.notification.enums.NotificationType;
import com.project.itda.domain.notification.repository.NotificationRepository;
import com.project.itda.domain.user.entity.User;
import com.project.itda.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 알림 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final PushNotificationService pushNotificationService;

    // ==================== 조회 ====================

    /**
     * 알림 목록 조회 (페이징)
     */
    public NotificationListResponse getNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notificationPage = notificationRepository.findByUserId(userId, pageable);

        Page<NotificationResponse> responsePage = notificationPage.map(NotificationResponse::from);
        Long unreadCount = notificationRepository.countUnreadByUserId(userId);

        return NotificationListResponse.of(responsePage, unreadCount);
    }

    /**
     * 읽지 않은 알림 목록 조회
     */
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findUnreadByUserId(userId);
        return notifications.stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 읽지 않은 알림 개수 조회
     */
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    /**
     * 알림 유형별 조회
     */
    public NotificationListResponse getNotificationsByType(Long userId, NotificationType type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notificationPage = notificationRepository.findByUserIdAndType(userId, type, pageable);

        Page<NotificationResponse> responsePage = notificationPage.map(NotificationResponse::from);
        Long unreadCount = notificationRepository.countUnreadByUserId(userId);

        return NotificationListResponse.of(responsePage, unreadCount);
    }

    /**
     * 단일 알림 조회
     */
    public NotificationResponse getNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다: " + notificationId));
        return NotificationResponse.from(notification);
    }

    // ==================== 읽음 처리 ====================

    /**
     * 단일 알림 읽음 처리
     */
    @Transactional
    public boolean markAsRead(Long notificationId, Long userId) {
        int updated = notificationRepository.markAsRead(notificationId, userId, LocalDateTime.now());
        if (updated > 0) {
            log.info("알림 읽음 처리 완료 - notificationId: {}, userId: {}", notificationId, userId);
            return true;
        }
        log.warn("알림 읽음 처리 실패 - notificationId: {}, userId: {}", notificationId, userId);
        return false;
    }

    /**
     * 모든 알림 읽음 처리
     */
    @Transactional
    public int markAllAsRead(Long userId) {
        int updated = notificationRepository.markAllAsReadByUserId(userId, LocalDateTime.now());
        log.info("전체 알림 읽음 처리 완료 - userId: {}, count: {}", userId, updated);
        return updated;
    }

    // ==================== 생성 ====================

    /**
     * 알림 생성
     */
    @Transactional
    public NotificationResponse createNotification(NotificationCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + request.getUserId()));

        Notification notification = Notification.builder()
                .user(user)
                .notificationType(request.getNotificationType())
                .title(request.getTitle())
                .content(request.getContent())
                .linkUrl(request.getLinkUrl())
                .relatedId(request.getRelatedId())
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("알림 생성 완료 - notificationId: {}, userId: {}, type: {}",
                saved.getNotificationId(), request.getUserId(), request.getNotificationType());

        return NotificationResponse.from(saved);
    }

    // ==================== 알림 생성 헬퍼 메서드 ====================

    /**
     * 모임 참가 알림 (DB + 푸시)
     */
    @Transactional
    public void notifyMeetingJoin(Long hostUserId, String participantName, Long meetingId, String meetingTitle) {
        NotificationCreateRequest request = NotificationCreateRequest.builder()
                .userId(hostUserId)
                .notificationType(NotificationType.MEETING)
                .title(participantName + "님이 모임에 참가했어요!")
                .content("💡 " + meetingTitle + " 모임에 새로운 멤버가 참가했습니다.")
                .linkUrl("/meeting/" + meetingId)
                .relatedId(meetingId)
                .build();
        createNotification(request);

        // 푸시 알림
        pushNotificationService.pushMeetingJoin(hostUserId, participantName, meetingId, meetingTitle);
    }

    /**
     * 모임 D-1 리마인더 알림 (DB + 푸시)
     */
    @Transactional
    public void notifyMeetingReminder(Long userId, Long meetingId, String meetingTitle, String meetingTime, String location) {
        NotificationCreateRequest request = NotificationCreateRequest.builder()
                .userId(userId)
                .notificationType(NotificationType.MEETING)
                .title(meetingTitle + " 모임이 내일입니다!")
                .content("📅 " + meetingTime + "에 " + location + "에서 만나요")
                .linkUrl("/meeting/" + meetingId)
                .relatedId(meetingId)
                .build();
        createNotification(request);

        // 푸시 알림
        pushNotificationService.pushMeetingReminder(userId, meetingId, meetingTitle, meetingTime, location);
    }

    /**
     * 후기 작성 알림 (DB + 푸시)
     */
    @Transactional
    public void notifyReview(Long hostUserId, String reviewerName, Long meetingId, String meetingTitle, int rating) {
        String stars = "★".repeat(rating) + "☆".repeat(5 - rating);
        NotificationCreateRequest request = NotificationCreateRequest.builder()
                .userId(hostUserId)
                .notificationType(NotificationType.REVIEW)
                .title(reviewerName + "님이 후기를 작성했어요!")
                .content("⭐ " + stars + " - " + meetingTitle)
                .linkUrl("/meeting/" + meetingId + "/reviews")
                .relatedId(meetingId)
                .build();
        createNotification(request);

        // 푸시 알림
        pushNotificationService.pushReview(hostUserId, reviewerName, meetingId, meetingTitle, rating);
    }

    /**
     * 팔로우 알림 (DB + 푸시)
     */
    @Transactional
    public void notifyFollow(Long targetUserId, String followerName, Long followerId) {
        NotificationCreateRequest request = NotificationCreateRequest.builder()
                .userId(targetUserId)
                .notificationType(NotificationType.FOLLOW)
                .title(followerName + "님이 회원님을 팔로우했어요!")
                .content("👤 새로운 팔로워가 생겼습니다.")
                .linkUrl("/profile/" + followerId)
                .relatedId(followerId)
                .build();
        createNotification(request);

        // 푸시 알림
        pushNotificationService.pushFollow(targetUserId, followerName, followerId);
    }

    /**
     * 배지 획득 알림 (DB + 푸시)
     */
    @Transactional
    public void notifyBadge(Long userId, Long badgeId, String badgeName, String badgeDescription) {
        NotificationCreateRequest request = NotificationCreateRequest.builder()
                .userId(userId)
                .notificationType(NotificationType.BADGE)
                .title("🏆 " + badgeName + " 배지를 획득했어요!")
                .content("🔥 " + badgeDescription)
                .linkUrl("/mypage/badges")
                .relatedId(badgeId)
                .build();
        createNotification(request);

        // 푸시 알림
        pushNotificationService.pushBadge(userId, badgeId, badgeName, badgeDescription);
    }

    /**
     * 채팅 알림 (DB + 푸시)
     */
    @Transactional
    public void notifyChat(Long userId, Long chatroomId, String senderName, String message) {
        String preview = message.length() > 30 ? message.substring(0, 30) + "..." : message;
        NotificationCreateRequest request = NotificationCreateRequest.builder()
                .userId(userId)
                .notificationType(NotificationType.CHAT)
                .title(senderName + "님의 새 메시지")
                .content("💬 " + preview)
                .linkUrl("/chatroom/" + chatroomId)
                .relatedId(chatroomId)
                .build();
        createNotification(request);

        // 푸시 알림
        pushNotificationService.pushChat(userId, chatroomId, senderName, message);
    }

    // ==================== 삭제 ====================

    /**
     * 단일 알림 삭제
     */
    @Transactional
    public boolean deleteNotification(Long notificationId, Long userId) {
        int deleted = notificationRepository.deleteByIdAndUserId(notificationId, userId);
        if (deleted > 0) {
            log.info("알림 삭제 완료 - notificationId: {}, userId: {}", notificationId, userId);
            return true;
        }
        log.warn("알림 삭제 실패 - notificationId: {}, userId: {}", notificationId, userId);
        return false;
    }

    /**
     * 읽은 알림 전체 삭제
     */
    @Transactional
    public int deleteReadNotifications(Long userId) {
        int deleted = notificationRepository.deleteReadNotificationsByUserId(userId);
        log.info("읽은 알림 삭제 완료 - userId: {}, count: {}", userId, deleted);
        return deleted;
    }

    // ==================== 스케줄링 ====================

    /**
     * 매일 새벽 3시에 30일 지난 알림 삭제
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOldNotifications() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        int deleted = notificationRepository.deleteOldNotifications(cutoffDate);
        log.info("오래된 알림 정리 완료 - 삭제 개수: {}", deleted);
    }
}