package com.project.itda.domain.notification.dto.request;

import com.project.itda.domain.notification.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 알림 생성 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationCreateRequest {

    private Long userId;

    @NotNull(message = "알림 유형은 필수입니다")
    private NotificationType notificationType;

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자 이내여야 합니다")
    private String title;

    @NotBlank(message = "내용은 필수입니다")
    private String content;

    @Size(max = 500, message = "링크 URL은 500자 이내여야 합니다")
    private String linkUrl;

    private Long relatedId;
}