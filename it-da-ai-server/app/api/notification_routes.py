"""
알림 API 라우터 (FastAPI Endpoints)
클라이언트가 호출할 수 있는 API 엔드포인트 정의
"""

from fastapi import APIRouter, Depends, HTTPException, Query, Path
from sqlalchemy.orm import Session
from typing import Optional

# 스키마 import
from app.schemas.notification import (
    NotificationCreate,
    NotificationMarkRead,
    NotificationResponse,
    NotificationListResponse,
    UnreadCountResponse,
    SuccessResponse
)

# 서비스 import
from app.services.notification_service import NotificationService

# 데이터베이스 세션 (이미 정의되어 있다고 가정)
# from app.core.database import get_db


# ========================================
# 라우터 생성
# ========================================

router = APIRouter(
    prefix="/api/notifications",
    tags=["Notifications"],
    responses={
        404: {"description": "알림을 찾을 수 없습니다"},
        400: {"description": "잘못된 요청입니다"}
    }
)


# ========================================
# 헬퍼 함수: 서비스 가져오기
# ========================================

def get_notification_service(db: Session = Depends(get_db)) -> NotificationService:
    """
    NotificationService 의존성 주입

    FastAPI의 Depends를 사용하여 자동으로 서비스 생성
    """
    return NotificationService(db)


# ========================================
# 1. 알림 생성 API
# ========================================

@router.post(
    "",
    response_model=NotificationResponse,
    status_code=201,
    summary="알림 생성",
    description="새로운 알림을 생성합니다."
)
def create_notification(
        notification_data: NotificationCreate,
        service: NotificationService = Depends(get_notification_service)
):
    """
    **알림 생성 API**

    - **user_id**: 알림 받을 사용자 ID (필수)
    - **notification_type**: 알림 타입 (MEETING/CHAT/REVIEW/BADGE/SYSTEM/FOLLOW)
    - **title**: 알림 제목 (필수)
    - **content**: 알림 내용 (필수)
    - **link_url**: 클릭 시 이동할 URL (선택)
    - **related_id**: 관련 ID (선택)

    **예시 요청:**
    ```json
    {
        "user_id": 1,
        "notification_type": "BADGE",
        "title": "🏆 새 배지 획득!",
        "content": "열정러 배지를 획득했어요!",
        "link_url": "/badges/participate_10",
        "related_id": 10
    }
    ```
    """

    try:
        result = service.create_notification(notification_data)
        return result
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"알림 생성 실패: {str(e)}"
        )


# ========================================
# 2. 알림 목록 조회 API
# ========================================

@router.get(
    "",
    response_model=NotificationListResponse,
    summary="알림 목록 조회",
    description="사용자의 알림 목록을 조회합니다."
)
def get_notifications(
        user_id: int = Query(..., description="사용자 ID", gt=0),
        unread_only: bool = Query(False, description="읽지 않은 알림만 조회할지 여부"),
        limit: int = Query(20, description="한 번에 가져올 개수", ge=1, le=100),
        offset: int = Query(0, description="건너뛸 개수 (페이지네이션)", ge=0),
        service: NotificationService = Depends(get_notification_service)
):
    """
    **알림 목록 조회 API**

    사용자의 알림 목록을 최신순으로 조회합니다.

    **파라미터:**
    - **user_id**: 사용자 ID (필수)
    - **unread_only**: `true`면 읽지 않은 알림만, `false`면 전체 (기본: false)
    - **limit**: 한 번에 가져올 개수 (기본: 20, 최대: 100)
    - **offset**: 건너뛸 개수 - 페이지네이션용 (기본: 0)

    **예시 요청:**
    ```
    GET /api/notifications?user_id=1&unread_only=true&limit=10
    ```

    **예시 응답:**
    ```json
    {
        "total_count": 10,
        "unread_count": 3,
        "notifications": [...]
    }
    ```
    """

    try:
        result = service.get_notifications(
            user_id=user_id,
            unread_only=unread_only,
            limit=limit,
            offset=offset
        )
        return result
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"알림 목록 조회 실패: {str(e)}"
        )


# ========================================
# 3. 알림 1개 조회 API
# ========================================

@router.get(
    "/{notification_id}",
    response_model=NotificationResponse,
    summary="알림 상세 조회",
    description="특정 알림 1개를 상세 조회합니다."
)
def get_notification(
        notification_id: int = Path(..., description="알림 ID", gt=0),
        user_id: int = Query(..., description="사용자 ID", gt=0),
        service: NotificationService = Depends(get_notification_service)
):
    """
    **알림 상세 조회 API**

    특정 알림 1개의 상세 정보를 조회합니다.

    **예시 요청:**
    ```
    GET /api/notifications/123?user_id=1
    ```
    """

    result = service.get_notification_by_id(notification_id, user_id)

    if not result:
        raise HTTPException(
            status_code=404,
            detail="알림을 찾을 수 없습니다."
        )

    return result


# ========================================
# 4. 읽음 처리 API
# ========================================

@router.post(
    "/mark-read",
    response_model=SuccessResponse,
    summary="알림 읽음 처리",
    description="선택한 알림들을 읽음 처리합니다."
)
def mark_notifications_as_read(
        data: NotificationMarkRead,
        user_id: int = Query(..., description="사용자 ID", gt=0),
        service: NotificationService = Depends(get_notification_service)
):
    """
    **알림 읽음 처리 API**

    선택한 알림들을 읽음 상태로 변경합니다.

    **예시 요청:**
    ```json
    {
        "notification_ids": [1, 2, 3]
    }
    ```

    **예시 응답:**
    ```json
    {
        "success": true,
        "message": "알림을 읽음 처리했습니다.",
        "affected_count": 3
    }
    ```
    """

    try:
        affected_count = service.mark_as_read(
            notification_ids=data.notification_ids,
            user_id=user_id
        )

        return SuccessResponse(
            success=True,
            message="알림을 읽음 처리했습니다.",
            affected_count=affected_count
        )
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"읽음 처리 실패: {str(e)}"
        )


# ========================================
# 5. 전체 읽음 처리 API
# ========================================

@router.post(
    "/mark-all-read",
    response_model=SuccessResponse,
    summary="전체 읽음 처리",
    description="모든 알림을 읽음 처리합니다."
)
def mark_all_notifications_as_read(
        user_id: int = Query(..., description="사용자 ID", gt=0),
        service: NotificationService = Depends(get_notification_service)
):
    """
    **전체 읽음 처리 API**

    사용자의 모든 알림을 읽음 처리합니다.

    **예시 요청:**
    ```
    POST /api/notifications/mark-all-read?user_id=1
    ```
    """

    try:
        affected_count = service.mark_all_as_read(user_id)

        return SuccessResponse(
            success=True,
            message="모든 알림을 읽음 처리했습니다.",
            affected_count=affected_count
        )
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"전체 읽음 처리 실패: {str(e)}"
        )


# ========================================
# 6. 알림 삭제 API
# ========================================

@router.delete(
    "/{notification_id}",
    response_model=SuccessResponse,
    summary="알림 삭제",
    description="특정 알림을 삭제합니다."
)
def delete_notification(
        notification_id: int = Path(..., description="알림 ID", gt=0),
        user_id: int = Query(..., description="사용자 ID", gt=0),
        service: NotificationService = Depends(get_notification_service)
):
    """
    **알림 삭제 API**

    특정 알림 1개를 삭제합니다.

    **예시 요청:**
    ```
    DELETE /api/notifications/123?user_id=1
    ```
    """

    success = service.delete_notification(notification_id, user_id)

    if not success:
        raise HTTPException(
            status_code=404,
            detail="알림을 찾을 수 없습니다."
        )

    return SuccessResponse(
        success=True,
        message="알림을 삭제했습니다.",
        affected_count=1
    )


# ========================================
# 7. 여러 알림 삭제 API
# ========================================

@router.post(
    "/delete-multiple",
    response_model=SuccessResponse,
    summary="여러 알림 삭제",
    description="선택한 알림들을 삭제합니다."
)
def delete_multiple_notifications(
        data: NotificationMarkRead,  # 같은 구조 재사용 (notification_ids 필드)
        user_id: int = Query(..., description="사용자 ID", gt=0),
        service: NotificationService = Depends(get_notification_service)
):
    """
    **여러 알림 삭제 API**

    선택한 알림들을 한 번에 삭제합니다.

    **예시 요청:**
    ```json
    {
        "notification_ids": [1, 2, 3]
    }
    ```
    """

    try:
        affected_count = service.delete_notifications(
            notification_ids=data.notification_ids,
            user_id=user_id
        )

        return SuccessResponse(
            success=True,
            message="알림을 삭제했습니다.",
            affected_count=affected_count
        )
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"알림 삭제 실패: {str(e)}"
        )


# ========================================
# 8. 읽지 않은 알림 개수 조회 API
# ========================================

@router.get(
    "/unread/count",
    response_model=UnreadCountResponse,
    summary="읽지 않은 알림 개수",
    description="읽지 않은 알림의 개수를 조회합니다."
)
def get_unread_count(
        user_id: int = Query(..., description="사용자 ID", gt=0),
        service: NotificationService = Depends(get_notification_service)
):
    """
    **읽지 않은 알림 개수 조회 API**

    사용자의 읽지 않은 알림 개수를 반환합니다.

    **예시 요청:**
    ```
    GET /api/notifications/unread/count?user_id=1
    ```

    **예시 응답:**
    ```json
    {
        "unread_count": 5
    }
    ```

    **사용 시나리오:**
    - 헤더의 알림 뱃지에 개수 표시
    - 실시간으로 새 알림 개수 확인
    """

    try:
        result = service.get_unread_count(user_id)
        return result
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"개수 조회 실패: {str(e)}"
        )


# ========================================
# 9. 오래된 알림 삭제 API (관리자용)
# ========================================

@router.delete(
    "/cleanup/old",
    response_model=SuccessResponse,
    summary="오래된 알림 삭제 (관리자)",
    description="오래된 읽은 알림을 자동으로 삭제합니다."
)
def cleanup_old_notifications(
        days: int = Query(30, description="며칠 이전 알림을 삭제할지", ge=1, le=365),
        # admin_key: str = Query(..., description="관리자 키"),  # 보안을 위해 추가
        service: NotificationService = Depends(get_notification_service)
):
    """
    **오래된 알림 삭제 API (관리자용)**

    지정한 일수보다 오래된 읽은 알림을 삭제합니다.

    **주의:** 이 API는 관리자만 사용해야 합니다!

    **예시 요청:**
    ```
    DELETE /api/notifications/cleanup/old?days=30
    ```

    **기본값:** 30일 이전의 읽은 알림 삭제
    """

    # TODO: 관리자 권한 체크 로직 추가
    # if admin_key != "your_secret_admin_key":
    #     raise HTTPException(status_code=403, detail="권한이 없습니다.")

    try:
        affected_count = service.delete_old_notifications(days)

        return SuccessResponse(
            success=True,
            message=f"{days}일 이전의 읽은 알림을 삭제했습니다.",
            affected_count=affected_count
        )
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"정리 실패: {str(e)}"
        )


# ========================================
# 실생활 비유로 이해하기 🏠
# ========================================

"""
📬 우편함 관리 창구 (API 엔드포인트)

고객(프론트엔드)이 창구 직원(API)에게 요청하면,
직원이 우편함 관리자(Service)에게 전달해서 처리합니다.

1. POST /api/notifications
   고객: "새 편지 하나 넣어주세요!"
   직원: "알겠습니다. 어떤 내용인가요?"
   
2. GET /api/notifications?user_id=1
   고객: "제 우편함에 편지 뭐 있나요?"
   직원: "확인해드리겠습니다. 총 10개, 안 읽은 거 3개 있네요!"
   
3. POST /api/notifications/mark-read
   고객: "이 편지들 읽었다고 표시해주세요."
   직원: "네, 3개 읽음 처리 완료했습니다!"
   
4. DELETE /api/notifications/123
   고객: "이 편지 좀 버려주세요."
   직원: "네, 삭제했습니다!"
   
5. GET /api/notifications/unread/count
   고객: "안 읽은 편지 몇 개예요?"
   직원: "5개 있습니다!"
"""


# ========================================
# 프론트엔드에서 사용하는 방법
# ========================================

"""
// React/TypeScript 예시

// 1. 알림 목록 조회
const fetchNotifications = async (userId: number) => {
    const response = await fetch(
        `/api/notifications?user_id=${userId}&limit=20`
    );
    const data = await response.json();
    console.log(data.notifications);
};

// 2. 읽지 않은 개수 조회
const fetchUnreadCount = async (userId: number) => {
    const response = await fetch(
        `/api/notifications/unread/count?user_id=${userId}`
    );
    const data = await response.json();
    return data.unread_count;
};

// 3. 읽음 처리
const markAsRead = async (notificationIds: number[], userId: number) => {
    const response = await fetch(
        `/api/notifications/mark-read?user_id=${userId}`,
        {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ notification_ids: notificationIds })
        }
    );
    const data = await response.json();
    console.log(`${data.affected_count}개 읽음 처리`);
};

// 4. 알림 삭제
const deleteNotification = async (notificationId: number, userId: number) => {
    const response = await fetch(
        `/api/notifications/${notificationId}?user_id=${userId}`,
        { method: 'DELETE' }
    );
    const data = await response.json();
    console.log(data.message);
};
"""