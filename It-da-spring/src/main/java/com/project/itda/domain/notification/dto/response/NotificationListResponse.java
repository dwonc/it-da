package com.project.itda.domain.notification.dto.response;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 알림 목록 응답 DTO (페이징 포함)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationListResponse {

    private List<NotificationResponse> notifications;
    private Long unreadCount;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int size;
    private boolean hasNext;
    private boolean hasPrevious;
    private boolean first;
    private boolean last;

    /**
     * Page + unreadCount로 생성
     */
    public static NotificationListResponse of(Page<NotificationResponse> page, Long unreadCount) {
        return NotificationListResponse.builder()
                .notifications(page.getContent())
                .unreadCount(unreadCount)
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .currentPage(page.getNumber())
                .size(page.getSize())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}