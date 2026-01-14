"""
알림 스키마 (Pydantic Models)
Request/Response 데이터 검증
"""

from pydantic import BaseModel, Field, ConfigDict
from typing import Optional, List
from datetime import datetime
from enum import Enum


# ========================================
# Enum: 알림 타입
# ========================================

class NotificationType(str, Enum):
    """알림 타입"""
    MEETING = "MEETING"      # 모임 관련
    CHAT = "CHAT"            # 채팅 메시지
    REVIEW = "REVIEW"        # 후기 관련
    BADGE = "BADGE"          # 배지 획득
    SYSTEM = "SYSTEM"        # 시스템 공지
    FOLLOW = "FOLLOW"        # 팔로우


# ========================================
# Request: 알림 생성
# ========================================

class NotificationCreate(BaseModel):
    """알림 생성 요청"""

    user_id: int = Field(..., description="사용자 ID", gt=0)
    notification_type: NotificationType = Field(..., description="알림 타입")
    title: str = Field(..., min_length=1, max_length=200, description="제목")
    content: str = Field(..., min_length=1, description="내용")
    link_url: Optional[str] = Field(None, max_length=500, description="연결 URL")
    related_id: Optional[int] = Field(None, description="관련 ID (모임ID, 배지ID 등)")

    model_config = ConfigDict(
        json_schema_extra={
            "example": {
                "user_id": 1,
                "notification_type": "BADGE",
                "title": "🏆 새 배지 획득!",
                "content": "열정러 배지를 획득했어요!",
                "link_url": "/badges/participate_10",
                "related_id": 10
            }
        }
    )


# ========================================
# Request: 알림 읽음 처리
# ========================================

class NotificationMarkRead(BaseModel):
    """알림 읽음 처리 요청"""

    notification_ids: List[int] = Field(..., description="읽음 처리할 알림 ID 목록")

    model_config = ConfigDict(
        json_schema_extra={
            "example": {
                "notification_ids": [1, 2, 3]
            }
        }
    )


# ========================================
# Response: 알림 상세
# ========================================

class NotificationResponse(BaseModel):
    """알림 응답"""

    notification_id: int = Field(..., description="알림 ID")
    user_id: int = Field(..., description="사용자 ID")
    notification_type: NotificationType = Field(..., description="알림 타입")
    title: str = Field(..., description="제목")
    content: str = Field(..., description="내용")
    link_url: Optional[str] = Field(None, description="연결 URL")
    related_id: Optional[int] = Field(None, description="관련 ID")
    is_read: bool = Field(..., description="읽음 여부")
    sent_at: datetime = Field(..., description="발송 시각")
    read_at: Optional[datetime] = Field(None, description="읽은 시각")

    model_config = ConfigDict(
        from_attributes=True,
        json_schema_extra={
            "example": {
                "notification_id": 1,
                "user_id": 1,
                "notification_type": "BADGE",
                "title": "🏆 새 배지 획득!",
                "content": "열정러 배지를 획득했어요!",
                "link_url": "/badges/participate_10",
                "related_id": 10,
                "is_read": False,
                "sent_at": "2026-01-13T10:30:00",
                "read_at": None
            }
        }
    )


# ========================================
# Response: 알림 목록
# ========================================

class NotificationListResponse(BaseModel):
    """알림 목록 응답"""

    total_count: int = Field(..., description="전체 알림 개수")
    unread_count: int = Field(..., description="읽지 않은 알림 개수")
    notifications: List[NotificationResponse] = Field(..., description="알림 목록")

    model_config = ConfigDict(
        json_schema_extra={
            "example": {
                "total_count": 10,
                "unread_count": 3,
                "notifications": [
                    {
                        "notification_id": 1,
                        "user_id": 1,
                        "notification_type": "BADGE",
                        "title": "🏆 새 배지 획득!",
                        "content": "열정러 배지를 획득했어요!",
                        "link_url": "/badges/participate_10",
                        "related_id": 10,
                        "is_read": False,
                        "sent_at": "2026-01-13T10:30:00",
                        "read_at": None
                    }
                ]
            }
        }
    )


# ========================================
# Response: 읽지 않은 알림 개수
# ========================================

class UnreadCountResponse(BaseModel):
    """읽지 않은 알림 개수 응답"""

    unread_count: int = Field(..., description="읽지 않은 알림 개수")

    model_config = ConfigDict(
        json_schema_extra={
            "example": {
                "unread_count": 5
            }
        }
    )


# ========================================
# Response: 성공 응답
# ========================================

class SuccessResponse(BaseModel):
    """성공 응답"""

    success: bool = Field(True, description="성공 여부")
    message: str = Field(..., description="메시지")
    affected_count: Optional[int] = Field(None, description="영향받은 개수")

    model_config = ConfigDict(
        json_schema_extra={
            "example": {
                "success": True,
                "message": "알림을 읽음 처리했습니다.",
                "affected_count": 3
            }
        }
    )
```

---

## ✅ 1번 파일 완료!

**작성한 내용:**
```
✅ NotificationType (Enum)
✅ NotificationCreate (생성 요청)
✅ NotificationMarkRead (읽음 처리 요청)
✅ NotificationResponse (알림 응답)
✅ NotificationListResponse (목록 응답)
✅ UnreadCountResponse (개수 응답)
✅ SuccessResponse (성공 응답)