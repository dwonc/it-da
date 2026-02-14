# 🚀 IT-DA (잇다)

## AI 기반 취향 매칭 플랫폼

> GPT 기반 Intent 분석과 LightGBM Scoring을 결합한  
> AI 추천 중심 모임 매칭 서비스

---

# 📌 1. 프로젝트 개요

IT-DA는 사용자의 자연어 요청을 분석하여  
취향과 의도를 구조화하고, AI 기반 Match Score를 산출하는 모임 추천 플랫폼입니다.

단순 카테고리 필터링이 아닌  
**AI 기반 점수 계산 구조 설계**를 통해 개인화된 추천 경험을 구현했습니다.

---

# 🏗 2. 시스템 아키텍처

## 전체 구조

```mermaid
flowchart TD
    A[React Frontend] --> B[Spring Boot API]
    B --> C[Redis Session]
    B --> D[MySQL RDS]
    B --> E[FastAPI AI Server]
    E --> F[GPT Intent Parsing]
    E --> G[Feature Engineering]
    E --> H[LightGBM Model]
    H --> I[Match Score %]
    I --> B
역할 분리 설계
🔹 Spring Boot
JWT 기반 인증 (Spring Security)

모임 CRUD

WebSocket 실시간 채팅

Redis 세션 관리

AI 서버 연동 및 결과 병합

🔹 FastAPI (AI Server)
GPT 기반 Intent Parsing

사용자 요청 구조화

Feature Engineering

LightGBM 기반 만족도 예측

Match Score 정규화

🤖 3. AI 추천 시스템 설계
추천 흐름
다이어그램
sequenceDiagram
    participant U as User
    participant FE as Frontend
    participant BE as Spring
    participant AI as FastAPI
    participant GPT as OpenAI
    participant LGBM as LightGBM

    U->>FE: 자연어 입력
    FE->>BE: 추천 요청
    BE->>AI: 사용자 데이터 전달
    AI->>GPT: Intent 분석 요청
    GPT-->>AI: 키워드 반환
    AI->>LGBM: Feature 입력
    LGBM-->>AI: 예측 점수 반환
    AI-->>BE: Match Score 전달
    BE-->>FE: 추천 결과 반환
Match Score 산출 기준
Match Score는 다음 요소를 기반으로 계산됩니다:

카테고리 적합도

분위기(Vibe) 유사도

거리 기반 가중치

사용자 선호도

LightGBM 예측 만족도

최종 점수는 0~100%로 정규화되어 사용자에게 제공됩니다.

🔥 4. 트러블슈팅 사례
4.1 AI 추천 점수 왜곡 문제
문제 현상
Match%가 40~60 구간에 과도 집중

특정 카테고리 반복 추천

원인 분석
Feature 스케일 불균형

Intent 가중치 과도 반영

점수 정규화 로직 단순화

개선 과정
Feature Scaling 재조정

Intent Weight 재설계

점수 분포 기반 튜닝

결과
점수 분포 정상화

추천 변별력 향상

사용자 체감 추천 정확도 개선

4.2 FastAPI 422 Validation Error
문제 현상
Spring → FastAPI 요청 시 422 오류 지속 발생

원인 분석
camelCase ↔ snake_case 불일치

DTO 구조 비일관성

Pydantic schema validation 실패

개선 과정
DTO 규격 통일

Pydantic alias 설정 적용

Request/Response 구조 표준화

로깅 기반 요청 데이터 추적

결과
API 성공률 안정화

인터페이스 정합성 확보

☁ 5. 배포 및 인프라 구조
다이어그램
flowchart LR
    User --> Domain[Gabia Domain]
    Domain --> FE[Vercel Frontend]
    FE --> BE[AWS Elastic Beanstalk]
    BE --> DB[RDS MySQL]
    BE --> Redis
운영 중 발생한 문제
AWS 4xx 오류 급증
API prefix 중복 설정

환경 변수 누락

EB 재배포 시 설정 초기화

→ 로그 기반 원인 추적 후 구조 수정

RDS 접근 불가 문제
RDS Private Subnet 위치 확인

VPC / Security Group 분석

EC2 SSH 접속 후 내부 DB 접근 테스트

→ 인프라 레벨 문제 해결 경험 확보

🧠 6. 설계 의도
왜 AI 서버를 분리했는가?
모델 추론 로직과 비즈니스 로직 분리

Python 생태계 활용

확장성과 유지보수성 확보

왜 Match% 정규화 구조를 도입했는가?
사용자 이해도 향상

추천 결과 설명 가능성 확보

점수 왜곡 최소화

📈 7. 향후 개선 방향
A/B 테스트 기반 추천 정확도 개선

Redis 캐싱 전략 고도화

Docker 멀티 컨테이너 구성

Kubernetes 기반 확장 구조 학습

🎯 8. 프로젝트를 통해 얻은 것
AI 모델은 연동 대상이 아니라 설계 대상이다.

인터페이스 정합성이 마이크로서비스의 핵심이다.

운영 환경 문제는 로그 기반 분석이 필수다.

추천 시스템은 가중치 설계가 가장 중요하다.
```
