## Warmplace Backend

- **Framework**: Spring Boot
- **Language**: Java 17+
- **Build Tool**: Maven/Gradle (프로젝트 설정에 따라)
- **Database**: PostgreSQL
- **Messaging**: Kafka (채팅 메시지 처리)
- **Security**: Spring Security + JWT

프론트엔드(`warmplace` 루트)와 함께 동작하는 백엔드 API 서버입니다.  
인증/갤러리/게시글/댓글/채팅(WebSocket + Kafka) 기능을 제공합니다.

---

### 1. 실행 전 준비

- Java 17 이상
- PostgreSQL 인스턴스
- Kafka 클러스터 (채팅 메시지용, 옵션: 비활성화 가능 여부는 설정에 따라)

필요한 기본 설정은 `application.yml` 또는 `application.properties` 에 정의합니다.  
대략 다음 정보가 필요합니다.

- DB 접속 정보 (`spring.datasource.*`)
- JPA 설정 (`spring.jpa.*`)
- JWT 시크릿/만료 설정
- Kafka 브로커 주소/토픽 이름 (`kafka.topic.chat`, `kafka.topic.group-chat` 등)

---

### 2. 빌드 & 실행

Maven 기준 예시:

```bash
cd warmplace_back

# 빌드
mvn clean package

# 실행
mvn spring-boot:run
```

Gradle 기준 예시:

```bash
cd warmplace_back

./gradlew clean build
./gradlew bootRun
```

서버 기본 포트는 `8081` 로 가정합니다.  
프론트엔드는 `http://localhost:8081` 에 요청을 보냅니다.

---

### 3. 주요 도메인 및 엔드포인트 (요약)

#### 3.1 인증 (`/api/auth`)

- `POST /api/auth/signup` – 회원가입
- `POST /api/auth/login` – 로그인 (JWT 발급)
- `POST /api/auth/logout` – 로그아웃
- `GET /api/auth/me` – 현재 사용자 정보 조회
- `GET /api/auth/exists?username={username}` – 사용자 존재 여부 확인

#### 3.2 갤러리 (`/api/galleries`)

- `GET /api/galleries` – 모든 갤러리 조회
- `GET /api/galleries/{id}` – 갤러리 상세 조회
- `GET /api/galleries/category/{category}` – 카테고리별 갤러리 조회
- `POST /api/galleries` – 갤러리 생성
- `PUT /api/galleries/{id}` – 갤러리 수정
- `DELETE /api/galleries/{id}` – 갤러리 삭제

#### 3.3 게시글 (`/api/posts`)

- `GET /api/posts` – 전체 게시글 조회
- `GET /api/posts/{id}` – 게시글 상세 조회
- `GET /api/posts/gallery/{galleryId}` – 특정 갤러리의 게시글 목록
- `POST /api/posts` – 게시글 생성 (인증 필요)
- `PUT /api/posts/{id}` – 게시글 수정
- `POST /api/posts/{id}/like` – 좋아요 증가
- `DELETE /api/posts/{id}` – 게시글 삭제

#### 3.4 댓글 (`/api/comments`)

- `GET /api/comments/post/{postId}` – 게시글의 댓글 목록 조회
- `POST /api/comments` – 댓글 생성 (인증 필요)
- `PUT /api/comments/{id}` – 댓글 내용 수정
- `DELETE /api/comments/{id}` – 댓글 삭제

#### 3.5 채팅 (`/api/chat`, WebSocket `/ws`)

- REST:
  - `POST /api/chat/direct` – 1:1 채팅 메시지 전송
  - `POST /api/chat/group` – 그룹 채팅 메시지 전송
  - `POST /api/chat/typing` – 1:1 입력 중 알림
  - `POST /api/chat/group/typing` – 그룹 입력 중 알림
  - `POST /api/chat/join` – 그룹 방 입장
  - `POST /api/chat/leave` – 그룹 방 퇴장
  - `GET /api/chat/direct/{userId1}/{userId2}` – 1:1 대화 내역 조회
  - `GET /api/chat/group/{roomId}` – 그룹 채팅 내역 조회
  - `GET /api/chat/rooms/{userId}` – 사용자의 채팅방 목록

- WebSocket/STOMP:
  - 엔드포인트: `/ws` (SockJS)
  - 사용자 큐: `/queue/{userId}/messages`
  - 그룹 토픽: `/topic/group/{roomId}`

Kafka Consumer/Producer 는 채팅 메시지를 DB(Mongo 등)에 저장하고,  
해당 사용자/방으로 메시지를 브로드캐스트합니다.

---

### 4. 보안 설정 개요 (`SecurityConfig`)

`SecurityConfig` 에서 JWT 기반 인증 및 CORS 를 설정합니다.

- 공개 경로 (인증 불필요):
  - `/api/auth/**`
  - `/ws/**`
  - `GET /api/galleries/**`
  - `GET /api/posts/**`
  - `GET /api/comments/**`
  - 스웨거/문서 경로: `/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`
- 이 외 엔드포인트는 JWT 토큰을 요구합니다.

JWT 필터는 `JwtAuthenticationFilter` 를 통해 `Authorization: Bearer <token>` 헤더를 파싱하고,  
`UserPrincipal` 로 인증 정보를 구성합니다.

---

### 5. 초기 데이터 시드 (`DataInitializer`)

`DataInitializer` 설정을 통해 서버 기동 시 다음 데이터를 자동으로 넣을 수 있습니다.

- 기본 갤러리 6개 (감성 카페, 자연 힐링 등)
- 여러 개의 게시글(Post)
- 예시 댓글(Comment) 들

단, 최소 1명의 사용자(`users` 테이블)가 존재해야 게시글/댓글 시드가 작성됩니다.  
사용자는 `/api/auth/signup` 으로 미리 하나 생성해 두는 것을 권장합니다.

---

### 6. 개발 시 참고

- 엔티티:
  - `Gallery`, `Post`, `Comment`, `User`, `ChatRoom`, `ChatMessageDocument` 등
- 서비스:
  - `AuthService`, `GalleryService`, `PostService`, `CommentService`, `ChatService`
- 컨트롤러:
  - `AuthController`, `GalleryController`, `PostController`, `CommentController`, `ChatController`

관련 클래스들을 참고하면서 프론트엔드의 API 호출부를 수정/확장하면 됩니다.

