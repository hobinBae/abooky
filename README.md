# 📖아북이 - 디지털 자서전 플랫폼

AI와 함께 만드는 개인 및 그룹 자서전 플랫폼입니다.
3D 한옥 환경에서 시작하여 몰입감 있는 스토리텔링 경험을 제공합니다.

---

## 🌟 주요 특징

- 🏠 **3D 한옥 인트로**: Three.js 기반 몰입형 시작 경험
- 📚 **개인 책 제작**: AI 도움으로 개인 자서전 작성
- 👥 **협업 책 제작**: WebRTC 기반 실시간 그룹 편집
- 🎨 **인터랙티브 편집기**: 드래그 앤 드롭, 이미지 업로드
- 📖 **디지털 서점**: 완성된 책 공유 및 탐색
- 🔐 **안전한 인증**: JWT 기반 사용자 관리

---

## 🛠 기술 스택

### Frontend

- **Frontend**: Vue 3 + TypeScript + Vite
- **상태관리**: Pinia
- **스타일링**: Bootstrap 5 + Custom CSS
- **3D 그래픽**: Three.js + GSAP
- **실시간 통신**: WebRTC (LiveKit), Server-Sent Events
- **인증**: JWT + OAuth (소셜 로그인)

### Backend

- **Java 17** - 프로그래밍 언어
- **Spring Boot 3.5.3** - 애플리케이션 프레임워크
- **Spring Security** - 인증/인가
- **Spring Data JPA** - 데이터 액세스
- **MySQL 8.0** - 메인 데이터베이스
- **Redis** - 캐싱 및 세션 관리
- **JWT** - 토큰 기반 인증

### AI & Communication

- **OpenAI API** - 지능형 질문 생성
- **Deepgram nova-2** - 음성-텍스트 변환
- **LiveKit** - 실시간 화상/음성 통신
- **Server-Sent Events (SSE)** - 실시간 업데이트

### Infrastructure

- **AWS S3** - 파일 저장
- **Docker Compose** - 개발 환경
- **Gradle** - 빌드 도구
- **Swagger** - API 문서화

---

## 📱 주요 기능

### 🏠 3D 인트로 경험

- 한옥 모델 기반 3D 환경
- 인터랙티브 핫스팟 탐색
- 부드러운 카메라 전환

### 📚 개인 책 제작

1. **책 생성**: AI 가이드로 테마 선택
    - STT 기술로 쉽게 답변 가능
    - 챕터 기반 인터뷰 시스템 : 5단계 구조화된 인터뷰
    - AI 기반 동적 질문 생성 : 사용자 답변 분석을 통한 맞춤형 후속 질문
    - 자동 교정 및 개선 제안
2. **챕터 편집**: 드래그 앤 드롭 편집기
3. **이미지 추가**: 파일 업로드 및 배치
4. **미리보기**: 실시간 책 미리보기
5. **출간**: 서점에 공개 또는 비공개 저장

### 👥 그룹 협업 책 제작

1. **그룹 생성**: 멤버 초대 및 역할 설정
2. **실시간 편집**: WebRTC 기반 동시 편집
3. **에피소드 구성** : 주어진 가이드에 따라 쉽게 에피소드 회상 가능
4. **타임라인:** 그룹 활동 히스토리

### 💌 커뮤니티 플랫폼

1. **개인책 출판** : 개인책 커뮤니티에 공유
2. **SNS 소통** : 좋아요, 댓글, 북마크 기능
3. **책 검색** : 책 제목, 작가 이름, 태그 기반 분류 시스템
4. **평점 및 리뷰 시스템**

### 🔐 사용자 관리

- 이메일 / 소셜 로그인 (Google)
- 프로필 관리 및 작가 페이지
- 개인 서재

---

## 📁 프로젝트 구조

- **frontend**
    
    ```jsx
    mybook/
    ├── src/
    │   ├── views/          # 페이지 컴포넌트
    │   │   ├── auth/       # 인증 관련
    │   │   ├── books/      # 개인 책 기능
    │   │   ├── groups/     # 그룹 책 기능
    │   │   └── general/    # 일반 페이지
    │   ├── components/     # 재사용 컴포넌트
    │   ├── stores/         # Pinia 상태 관리
    │   ├── services/       # API 서비스
    │   ├── router/         # Vue Router 설정
    │   └── api/            # API 클라이언트
    ├── public/
    │   └── 3D/             # 3D 모델 에셋
    └── docs/               # 문서
    ```
    
- **backend** - Domain 구조 (DDD 기반)
    
    ```jsx
    autobiography/domain/                                 
    │ │ ├── auth/                   # 인증 및 권한 관리     
    │ │ │   ├── controller/         # 로그인, 회원가입, 소셜로그인 API      
    │ │ │   ├── dto/                # 인증 관련 요청/응답DTO
    │ │ │   └── service/            # 인증 서비스, 이메일 서비스
    │ │ ├── member/                 # 사용자 관리
    │ │ │   ├── controller/         # 회원 정보 CRUD API
    │ │ │   ├── dto/                # 회원 관련 DTO
    │ │ │   ├── entity/             # Member 엔티티
    │ │ │   ├── repository/         # 회원 데이터 접근
    │ │ │   └── service/            # 회원 비즈니스 로직
    │ │ ├── book/                   # 개인 자서전 관리
    │ │ │   ├── controller/         # 책 CRUD, 카테고리 관리 API
    │ │ │   ├── dto/                # 책 관련 DTO
    │ │ │   ├── entity/             # Book, Category, Rating 엔티티
    │ │ │   ├── repository/         # 책 데이터 접근
    │ │ │   └── service/            # 책 비즈니스 로직
    │ │ ├── episode/                # 인터뷰 세션 관리
    │ │ │   ├── controller/         # 에피소드, 대화 API
    │ │ │   ├── dto/                # 에피소드 관련 DTO
    │ │ │   ├── entity/             # Episode, Conversation 엔티티
    │ │ │   ├── repository/         # 에피소드 데이터 접근
    │ │ │   ├── service/            # 에피소드 비즈니스 로직
    │ │ │   └── template/           # 챕터 기반 질문 템플릿 시스템
    │ │ │       ├── dto/            # 챕터 진행, 질문 DTO
    │ │ │       ├── entity/         # Chapter, Template 엔티티
    │ │ │       ├── repository/     # 템플릿 데이터 접근
    │ │ │       └── service/        # 챕터 관리 서비스
    │ │ ├── communityBook/          # 커뮤니티 자서전 공유
    │ │ │   ├── controller/         # 커뮤니티 책 조회, 검색, 좋아요 API
    │ │ │   ├── dto/                # 커뮤니티 책 관련 DTO
    │ │ │   ├── entity/             # CommunityBook, Like, Comment 엔티티
    │ │ │   ├── repository/         # 커뮤니티 책 데이터 접근
    │ │ │   └── service/            # 커뮤니티 책 비즈니스 로직
    │ │ ├── group/                  # 그룹 관리
    │ │ │   ├── controller/         # 그룹 생성, 멤버 관리 API
    │ │ │   ├── dto/                # 그룹 관련 DTO
    │ │ │   ├── entity/             # Group, Member, Apply 엔티티
    │ │ │   ├── repository/         # 그룹 데이터 접근
    │ │ │   └── service/            # 그룹 비즈니스 로직
    │ │ ├── groupbook/              # 협업 자서전 작성
    │ │ │   ├── controller/         # 그룹 책 관리 API
    │ │ │   ├── dto/                # 그룹 책 관련 DTO
    │ │ │   ├── entity/             # GroupBook 엔티티
    │ │ │   ├── repository/         # 그룹 책 데이터 접근
    │ │ │   ├── service/            # 그룹 책 비즈니스 로직
    │ │ │   └── episode/            # 그룹 인터뷰 세션
    │ │ │       ├── controller/     # 그룹 에피소드 관리 API
    │ │ │       ├── dto/            # 그룹 에피소드 DTO
    │ │ │       ├── entity/         # GroupEpisode 엔티티
    │ │ │       ├── repository/     # 그룹 에피소드 데이터 접근
    │ │ │       └── service/        # 가이드 질문, 편집 서비스
    │ │ ├── ai/                     # AI 통합
    │ │ │   ├── client/             # OpenAI API 클라이언트
    │ │ │   ├── controller/         # AI 관련 API
    │ │ │   ├── dto/                # AI 요청/응답 DTO
    │ │ │   └── service/            # AI 서비스 (질문 생성, 교정)
    │ │ ├── stt/                    # 음성-텍스트 변환
    │ │ │   ├── client/             # Whisper, LiveKit STT 클라이언트
    │ │ │   ├── controller/         # STT API
    │ │ │   ├── dto/                # STT 관련 DTO
    │ │ │   └── service/            # STT 비즈니스 로직
    │ │ ├── rtc/                    # 실시간 통신
    │ │ │   ├── controller/         # LiveKit RTC API
    │ │ │   ├── dto/                # RTC 관련 DTO
    │ │ │   └── service/            # 그룹 룸 관리 서비스
    │ │ └── sse/                    # 서버-전송 이벤트
    │ │     ├── controller/         # SSE API
    │ │     └── service/            # 실시간 업데이트 서비스
    ```

- **DB**
    ```jsx
    Member (회원)
    │ │ ├── Book (개인 자서전)
    │ │ │   ├── Episode (인터뷰 세션)
    │ │ │   │   ├── ConversationSession (대화 세션)
    │ │ │   │   └── ConversationMessage (대화 메시지)
    │ │ │   └── Rating (평점)
    │ │ ├── CommunityBook (공유 자서전)
    │ │ │   ├── CommunityBookLike (좋아요)
    │ │ │   ├── CommunityBookComment (댓글)
    │ │ │   ├── CommunityBookBookmark (북마크)
    │ │ │   └── CommunityBookRating (평점) 
    │ │ ├── Group (그룹)
    │ │ │   ├── GroupMember (그룹 멤버)
    │ │ │   ├── GroupApply (가입 신청)
    │ │ │   └── GroupBook (협업 자서전)
    │ │ │       └── GroupEpisode (그룹 인터뷰)
    │ │ └── ChapterTemplate (챕터 템플릿)
    │ │     └── FollowUpQuestion (후속 질문)
    ```