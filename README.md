# 🤝 IT-DA (잇다)

AI로 나한테 맞는 모임을 찾아주는 서비스입니다.  
"조용히 코딩하고 싶어"라고 입력하면 GPT가 의도를 파악하고, 비슷한 취향의 모임을 추천해줍니다.

---

## 📌 프로젝트 소개

**기간**: 2025.01 ~ 2025.02 (1개월)  
**인원**: 6명 (팀 리더)  
**역할**: 백엔드|프론트엔드 개발, AI 시스템 설계

**기술 스택**
- Backend: Spring Boot, FastAPI, MySQL, Redis
- AI: GPT API, LightGBM, SVD
- Frontend: React, TypeScript

---

## ✨ 주요 기능

### 1. AI 기반 모임 추천
사용자가 자연어로 원하는 걸 입력하면 AI가 분석해서 맞는 모임을 찾아줍니다.

**추천 과정**
1. 사용자 입력: "조용한 카페에서 책 읽는 모임 찾아줘" 💬
2. GPT가 의도 파악: "카페", "조용함", "독서" 🤖
3. LightGBM이 각 모임에 점수 부여 📊
4. Match Score 높은 순으로 정렬 ⭐

**점수 계산 방식**
- 카테고리 일치도: 25%
- 분위기 유사도: 30%
- 사용자 선호도: 45%

### 2. 실시간 채팅
WebSocket(STOMP)으로 모임 참여자들끼리 채팅할 수 있습니다. 💬

---

## 🏗️ 시스템 구조
React Frontend
↓
Spring Boot API (모임 CRUD, 채팅)
↓
FastAPI (AI 서버)
↓
GPT + LightGBM
**왜 AI 서버를 분리했나요?**
- Spring Boot에서 Python 쓰기 어려움
- AI 로직만 따로 고치기 편함

---

## 🔥 해결한 문제

### 1. 추천 점수가 비슷하게 나오는 문제

**문제 상황** 😵  
어떤 모임을 검색해도 점수가 다 50% 근처로 나왔습니다.  
"이게 나한테 맞는 건지 아닌지" 구분이 안 됐어요.

**원인**  
- Feature 값의 범위가 제각각이었음
- 특정 Feature만 점수에 영향을 줌

**해결** ✅  
1. 모든 Feature를 0~1로 정규화
2. Intent(의도) 가중치를 조정
3. 여러 번 테스트하면서 비율 맞춤

**결과**  
점수가 20%~90% 범위로 골고루 나옴 📈

---

### 2. FastAPI 422 에러

**문제 상황** ❌  
Spring Boot에서 FastAPI로 데이터 보내면 422 에러 발생

**원인**  
- Spring Boot: `userName` (camelCase)
- FastAPI: `user_name` (snake_case)
- 형식이 안 맞아서 검증 실패

**해결** ✅  
Pydantic에서 자동 변환하도록 설정

```python
class Config:
    alias_generator = to_camel
    populate_by_name = True
3. 실시간 채팅 메시지가 안 보이는 문제
문제 상황 💬
메시지를 보내면 DB에는 저장되는데 상대방한테 안 보임
원인
구독 경로: /topic/chat/{chatId}
메시지 전송 경로: /topic/chat/{userId}
경로가 달라서 못 받음
해결 ✅
경로를 통일해서 해결
4. SVD 추천이 이상하게 나오는 문제
문제 상황 🤔
협업 필터링(SVD)으로 추천했는데 전혀 관련 없는 모임이 나옴
원인
데이터가 너무 적음 (사용자 10명, 모임 20개)
Cold Start 문제
해결 ✅
SVD 비중을 낮추고 LightGBM 비중을 올림
LightGBM: 70%
SVD: 30%
💡 느낀 점
AI는 연동만 하면 끝이 아니다
GPT 결과를 어떻게 점수로 만들지, 어떻게 가중치를 줄지 계속 고민했습니다.
데이터가 적으면 머신러닝이 의미 없다
SVD 쓰려고 했는데 데이터가 너무 적어서 제대로 작동 안 했어요.
로그 없으면 문제 못 찾는다
422 에러 나왔을 때 로그 보고 데이터 형식이 다른 걸 찾았습니다.
🚀 실행 방법
# Backend
cd backend
./mvnw spring-boot:run

# AI Server
cd ai-service
pip install -r requirements.txt
uvicorn main:app --reload

# Frontend
cd frontend
npm install
npm start
👥 팀원
최동원 (팀장): 백엔드, 프론트엔드, AI 시스템 설계, 모임페이지
김보민: 백엔드, 프론트엔드, 마이페이지
김봉환: 백엔드, 프론트엔드, 관리자페이지
신의진: 백엔드, 프론트엔드, 모임채팅
김동민: 백엔드, 프론트엔드
박성훈: 백엔드, 프론트엔드
...
더 궁금한 점이 있으면 연락주세요! 📧
dwonc2@naver.com
---
