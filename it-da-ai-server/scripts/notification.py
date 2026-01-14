"""
알림 모듈 간단 테스트
MySQL 없이도 기본 동작 확인 가능!
"""

import sys
import os

# 프로젝트 루트를 Python 경로에 추가
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))


def test_schemas():
    """
    1단계: 스키마(데이터 검증) 테스트
    """
    print("=" * 60)
    print("🧪 테스트 1: 스키마 (데이터 검증)")
    print("=" * 60)

    try:
        from app.schemas.notification import (
            NotificationCreate,
            NotificationType,
            NotificationResponse
        )

        # 알림 생성 데이터
        data = NotificationCreate(
            user_id=1,
            notification_type=NotificationType.BADGE,
            title="🏆 열정러 배지 획득!",
            content="10회 모임 참여를 달성했어요!",
            link_url="/badges/participate_10",
            related_id=10
        )

        print(f"✅ 스키마 import 성공!")
        print(f"✅ 데이터 검증 성공!")
        print(f"\n생성된 데이터:")
        print(f"  - user_id: {data.user_id}")
        print(f"  - type: {data.notification_type}")
        print(f"  - title: {data.title}")
        print(f"  - content: {data.content}")

        return True

    except Exception as e:
        print(f"❌ 실패: {e}")
        return False


def test_model():
    """
    2단계: 모델(테이블 정의) 테스트
    """
    print("\n" + "=" * 60)
    print("🧪 테스트 2: 모델 (테이블 정의)")
    print("=" * 60)

    try:
        from app.models.notification_model import Notification, NotificationTypeEnum
        from datetime import datetime

        # 알림 객체 생성 (DB 없이 메모리에만)
        notification = Notification(
            notification_id=1,
            user_id=1,
            notification_type=NotificationTypeEnum.BADGE,
            title="🏆 열정러 배지 획득!",
            content="10회 모임 참여를 달성했어요!",
            link_url="/badges/participate_10",
            related_id=10,
            is_read=False,
            sent_at=datetime.now()
        )

        print(f"✅ 모델 import 성공!")
        print(f"✅ 객체 생성 성공!")
        print(f"\n객체 정보:")
        print(f"  {notification}")

        # to_dict 메서드 테스트
        dict_data = notification.to_dict()
        print(f"\n✅ to_dict() 메서드 작동!")
        print(f"  notification_id: {dict_data['notification_id']}")
        print(f"  is_read: {dict_data['is_read']}")

        # mark_as_read 메서드 테스트
        notification.mark_as_read()
        print(f"\n✅ mark_as_read() 메서드 작동!")
        print(f"  is_read: {notification.is_read}")
        print(f"  read_at: {notification.read_at}")

        return True

    except Exception as e:
        print(f"❌ 실패: {e}")
        import traceback
        traceback.print_exc()
        return False


def test_database_connection():
    """
    3단계: 데이터베이스 연결 테스트
    """
    print("\n" + "=" * 60)
    print("🧪 테스트 3: 데이터베이스 연결")
    print("=" * 60)

    try:
        from app.core.database import test_connection

        if test_connection():
            print("✅ 데이터베이스 연결 성공!")
            return True
        else:
            print("⚠️  데이터베이스 연결 실패 (MySQL 서버 확인 필요)")
            return False

    except Exception as e:
        print(f"❌ 실패: {e}")
        print("💡 힌트: MySQL 서버가 실행 중인지 확인하세요")
        return False


def test_imports():
    """
    4단계: 모든 import 테스트
    """
    print("\n" + "=" * 60)
    print("🧪 테스트 4: 전체 import")
    print("=" * 60)

    imports_to_test = [
        ("app.schemas.notification", "NotificationCreate"),
        ("app.models.notification_model", "Notification"),
        ("app.services.notification_service", "NotificationService"),
        ("app.api.notification_routes", "router"),
        ("app.core.database", "Base"),
    ]

    success_count = 0

    for module_name, class_name in imports_to_test:
        try:
            module = __import__(module_name, fromlist=[class_name])
            getattr(module, class_name)
            print(f"✅ {module_name}.{class_name}")
            success_count += 1
        except Exception as e:
            print(f"❌ {module_name}.{class_name} - {e}")

    print(f"\n결과: {success_count}/{len(imports_to_test)} 성공")

    return success_count == len(imports_to_test)


def run_all_tests():
    """
    모든 테스트 실행
    """
    print("\n" + "🚀" * 30)
    print("알림 모듈 테스트 시작!")
    print("🚀" * 30 + "\n")

    results = []

    # 테스트 실행
    results.append(("스키마", test_schemas()))
    results.append(("모델", test_model()))
    results.append(("Import", test_imports()))
    results.append(("DB 연결", test_database_connection()))

    # 최종 결과
    print("\n" + "=" * 60)
    print("📊 최종 결과")
    print("=" * 60)

    for name, success in results:
        status = "✅ 통과" if success else "❌ 실패"
        print(f"{name:15} : {status}")

    success_count = sum(1 for _, success in results if success)
    total_count = len(results)

    print("\n" + "=" * 60)
    print(f"총 {success_count}/{total_count} 테스트 통과")
    print("=" * 60)

    if success_count == total_count:
        print("\n🎉 모든 테스트 통과! 코드가 정상 작동합니다!")
    elif success_count >= total_count - 1:
        print("\n✅ 거의 완벽! DB 연결만 확인하면 됩니다!")
    else:
        print("\n⚠️  일부 테스트 실패. 에러 메시지를 확인하세요.")


if __name__ == "__main__":
    """
    실행 방법:
    
    cd it-da-ai-server
    python scripts/test_notification.py
    """
    run_all_tests()