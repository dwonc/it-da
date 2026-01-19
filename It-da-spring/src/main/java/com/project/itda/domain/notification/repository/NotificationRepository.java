package com.project.itda.domain.notification.repository;

import com.project.itda.domain.notification.entity.Notification;
import com.project.itda.domain.notification.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 알림 Repository
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // ==================== 조회 ====================

    /**
     * 사용자의 알림 목록 조회 (최신순, 페이징)
     */
    @Query("SELECT n FROM Notification n WHERE n.user.userId = :userId ORDER BY n.sentAt DESC")
    Page<Notification> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 사용자의 읽지 않은 알림 목록 조회 (최신순)
     */
    @Query("SELECT n FROM Notification n WHERE n.user.userId = :userId AND n.isRead = false ORDER BY n.sentAt DESC")
    List<Notification> findUnreadByUserId(@Param("userId") Long userId);

    /**
     * 사용자의 읽지 않은 알림 개수
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.userId = :userId AND n.isRead = false")
    Long countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 알림 유형별 조회 (페이징)
     */
    @Query("SELECT n FROM Notification n WHERE n.user.userId = :userId AND n.notificationType = :type ORDER BY n.sentAt DESC")
    Page<Notification> findByUserIdAndType(
            @Param("userId") Long userId,
            @Param("type") NotificationType type,
            Pageable pageable
    );

    /**
     * 사용자와 알림 ID로 조회 (권한 체크용)
     */
    @Query("SELECT n FROM Notification n WHERE n.notificationId = :notificationId AND n.user.userId = :userId")
    Optional<Notification> findByIdAndUserId(
            @Param("notificationId") Long notificationId,
            @Param("userId") Long userId
    );

    /**
     * 관련 ID와 유형으로 알림 조회
     */
    List<Notification> findByRelatedIdAndNotificationType(Long relatedId, NotificationType type);

    // ==================== 수정 ====================

    /**
     * 단일 알림 읽음 처리
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.notificationId = :notificationId AND n.user.userId = :userId AND n.isRead = false")
    int markAsRead(
            @Param("notificationId") Long notificationId,
            @Param("userId") Long userId,
            @Param("readAt") LocalDateTime readAt
    );

    /**
     * 사용자의 모든 알림 읽음 처리
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.user.userId = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);

    // ==================== 삭제 ====================

    /**
     * 오래된 알림 삭제 (배치용)
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.sentAt < :cutoffDate")
    int deleteOldNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * 사용자의 특정 알림 삭제
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.notificationId = :notificationId AND n.user.userId = :userId")
    int deleteByIdAndUserId(@Param("notificationId") Long notificationId, @Param("userId") Long userId);

    /**
     * 사용자의 읽은 알림 전체 삭제
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user.userId = :userId AND n.isRead = true")
    int deleteReadNotificationsByUserId(@Param("userId") Long userId);
}