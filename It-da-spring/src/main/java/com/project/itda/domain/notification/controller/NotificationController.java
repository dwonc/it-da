package com.project.itda.domain.notification.controller;

import com.project.itda.domain.notification.dto.request.NotificationCreateRequest;
import com.project.itda.domain.notification.dto.response.NotificationListResponse;
import com.project.itda.domain.notification.dto.response.NotificationResponse;
import com.project.itda.domain.notification.enums.NotificationType;
import com.project.itda.domain.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 알림 API 컨트롤러
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification", description = "알림 관련 API")
public class NotificationController {

    private final NotificationService notificationService;

    // ==================== 조회 ====================

    /**
     * 알림 목록 조회 (페이징)
     */
    @GetMapping
    @Operation(summary = "알림 목록 조회", description = "사용자의 알림 목록을 페이징하여 조회합니다")
    public ResponseEntity<NotificationListResponse> getNotifications(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size
    ) {
        log.info("알림 목록 조회 - userId: {}, page: {}, size: {}", userId, page, size);
        NotificationListResponse response = notificationService.getNotifications(userId, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * 읽지 않은 알림 목록 조회
     */
    @GetMapping("/unread")
    @Operation(summary = "읽지 않은 알림 조회", description = "읽지 않은 알림 목록을 조회합니다")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(
            @AuthenticationPrincipal Long userId
    ) {
        log.info("읽지 않은 알림 조회 - userId: {}", userId);
        List<NotificationResponse> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * 읽지 않은 알림 개수 조회
     */
    @GetMapping("/unread/count")
    @Operation(summary = "읽지 않은 알림 개수", description = "읽지 않은 알림의 개수를 조회합니다")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal Long userId
    ) {
        log.info("읽지 않은 알림 개수 조회 - userId: {}", userId);
        Long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * 알림 유형별 조회
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "유형별 알림 조회", description = "특정 유형의 알림 목록을 조회합니다")
    public ResponseEntity<NotificationListResponse> getNotificationsByType(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "알림 유형") @PathVariable NotificationType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("유형별 알림 조회 - userId: {}, type: {}", userId, type);
        NotificationListResponse response = notificationService.getNotificationsByType(userId, type, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * 단일 알림 조회
     */
    @GetMapping("/{notificationId}")
    @Operation(summary = "알림 상세 조회", description = "특정 알림의 상세 정보를 조회합니다")
    public ResponseEntity<NotificationResponse> getNotification(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "알림 ID") @PathVariable Long notificationId
    ) {
        log.info("알림 상세 조회 - userId: {}, notificationId: {}", userId, notificationId);
        NotificationResponse response = notificationService.getNotification(notificationId, userId);
        return ResponseEntity.ok(response);
    }

    // ==================== 읽음 처리 ====================

    /**
     * 단일 알림 읽음 처리
     */
    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 처리합니다")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "알림 ID") @PathVariable Long notificationId
    ) {
        log.info("알림 읽음 처리 - userId: {}, notificationId: {}", userId, notificationId);
        boolean success = notificationService.markAsRead(notificationId, userId);

        return ResponseEntity.ok(Map.of(
                "success", success,
                "message", success ? "알림을 읽음 처리했습니다" : "알림 읽음 처리에 실패했습니다"
        ));
    }

    /**
     * 모든 알림 읽음 처리
     */
    @PatchMapping("/read-all")
    @Operation(summary = "모든 알림 읽음 처리", description = "모든 알림을 읽음 처리합니다")
    public ResponseEntity<Map<String, Object>> markAllAsRead(
            @AuthenticationPrincipal Long userId
    ) {
        log.info("전체 알림 읽음 처리 - userId: {}", userId);
        int count = notificationService.markAllAsRead(userId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", count + "개의 알림을 읽음 처리했습니다",
                "count", count
        ));
    }

    // ==================== 삭제 ====================

    /**
     * 단일 알림 삭제
     */
    @DeleteMapping("/{notificationId}")
    @Operation(summary = "알림 삭제", description = "특정 알림을 삭제합니다")
    public ResponseEntity<Map<String, Object>> deleteNotification(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "알림 ID") @PathVariable Long notificationId
    ) {
        log.info("알림 삭제 - userId: {}, notificationId: {}", userId, notificationId);
        boolean success = notificationService.deleteNotification(notificationId, userId);

        return ResponseEntity.ok(Map.of(
                "success", success,
                "message", success ? "알림이 삭제되었습니다" : "알림 삭제에 실패했습니다"
        ));
    }

    /**
     * 읽은 알림 전체 삭제
     */
    @DeleteMapping("/read")
    @Operation(summary = "읽은 알림 전체 삭제", description = "읽은 알림을 모두 삭제합니다")
    public ResponseEntity<Map<String, Object>> deleteReadNotifications(
            @AuthenticationPrincipal Long userId
    ) {
        log.info("읽은 알림 전체 삭제 - userId: {}", userId);
        int count = notificationService.deleteReadNotifications(userId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", count + "개의 읽은 알림을 삭제했습니다",
                "count", count
        ));
    }

    // ==================== 테스트용 ====================

    /**
     * 테스트 알림 생성 (개발용 - 인증 불필요)
     */
    @PostMapping("/test")
    @Operation(summary = "테스트 알림 생성", description = "테스트용 알림을 생성합니다 (개발용)")
    public ResponseEntity<NotificationResponse> createTestNotification(
            @RequestParam Long userId,
            @Valid @RequestBody NotificationCreateRequest request
    ) {
        log.info("테스트 알림 생성 - userId: {}", userId);
        request.setUserId(userId);
        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}