# 꾸튜디오 Admin

여러 앱(첫 대상: 꼬리)을 통합 관리하는 어드민 모노레포.

## 기술 스택

| 영역 | 스택 | 버전 |
|---|---|---|
| admin-api | Java | 21 |
| admin-api | Spring Boot | 4.1.1 |
| admin-api | Gradle (wrapper) | 9.7.1 |
| admin-api | PostgreSQL | 16 |
| admin-api | Flyway | Spring Boot BOM 관리 |
| admin-api | Spring Session JDBC | 4.1.1 |
| admin-web | Node.js | 26 |
| admin-web | React | 19 |
| admin-web | TypeScript | 6.0 |
| admin-web | Vite | 8 |
| admin-web | Ant Design | 6 |
| admin-web | TanStack Query | 5 |
| admin-web | Recharts | 3 |
| infra | Docker Compose, nginx | - |

## 디렉터리 구조

```
kkutudio-admin/
├─ admin-api/              Spring Boot API 서버
│  ├─ src/main/java/com/kkutudio/admin/
│  │  ├─ auth/             관리자 인증·세션·로그인 이력 (SecurityConfig, AuthController, AdminBootstrapRunner)
│  │  ├─ application/      관리 대상 앱(managed_app/app_connection) 등록·조회 (ManagedAppController)
│  │  ├─ member/           앱 API를 통한 회원 조회·정지·강제 로그아웃 (MemberController)
│  │  ├─ statistics/       앱 API를 통한 대시보드 통계 실시간 조회 (StatisticsController)
│  │  ├─ audit/            관리자 작업 이력(audit_log) 기록 (AuditLogService)
│  │  ├─ integration/      앱 admin API 연동 클라이언트 (AppAdminClient, AppAdminClientFactory)
│  │  └─ common/           공통 설정·예외 (GlobalExceptionHandler, CsrfCookieFilter)
│  └─ src/main/resources/db/migration/   Flyway 마이그레이션 (V1~V3)
├─ admin-web/               React + Vite 어드민 프론트엔드
│  └─ src/
│     ├─ api/               admin-api 호출 (client, auth, applications, members, statistics)
│     ├─ auth/               AuthContext, RequireAuth
│     ├─ layout/             AppLayout (사이드바 + 상단 앱 선택기)
│     └─ pages/               LoginPage, MembersPage, AppIndexRedirect,
│                             dashboard/ (DashboardPage + 개요·회원·반려동물·기록·전환 탭)
├─ infra/
│  ├─ docker-compose.yml    postgres + admin-api + nginx
│  └─ nginx/                정적 파일 서빙 + /admin-api 리버스 프록시
├─ CLAUDE.md
└─ AGENTS.md
```

## 실행·빌드·테스트 명령

### admin-api

```bash
cd admin-api
./gradlew build          # 빌드 + 테스트(Testcontainers, Docker 필요)
./gradlew build -x test  # 테스트 제외 빌드
./gradlew bootRun         # 로컬 실행 (환경변수 필요, 아래 참고)
```

로컬 실행 시 필요한 환경변수 (기본값은 docker-compose와 동일):

- `ADMIN_DB_URL` (기본 `jdbc:postgresql://localhost:5432/kkutudio_admin`)
- `ADMIN_DB_USERNAME` (기본 `admin`)
- `ADMIN_DB_PASSWORD` (기본 `admin`)
- `ADMIN_API_PORT` (기본 `8080`)
- `ADMIN_BOOTSTRAP_EMAIL` / `ADMIN_BOOTSTRAP_PASSWORD` / `ADMIN_BOOTSTRAP_NAME`: `admin_user` 테이블이 비어 있을 때만 최초 1회 사용되는 초기 관리자 계정. 값이 없으면 계정을 만들지 않고 경고 로그만 남긴다(`AdminBootstrapRunner`). 비밀번호를 마이그레이션/코드에 커밋하지 않기 위한 장치이므로 실제 값은 로컬 `.env`에만 둔다.

서버 컨텍스트 경로는 `/admin-api` 로 고정되어 있다 (nginx·Vite 프록시와 경로 규칙을 맞추기 위함).

### admin-web

```bash
cd admin-web
npm install
npm run dev     # 개발 서버, /admin-api 요청은 localhost:8080으로 프록시
npm run build   # 프로덕션 빌드 (tsc -b && vite build)
npm run lint     # oxlint
```

### infra (전체 스택)

```bash
cd infra
cp .env.example .env   # 필요 시 값 수정
docker compose up --build
```

- `nginx` → http://localhost:8081 (정적 파일 서빙 + `/admin-api/*` 를 `admin-api` 컨테이너로 프록시)
- `admin-api` → 컨테이너 내부 8080, 외부 미노출 (nginx 경유)
- `postgres` → localhost:5432

## 아키텍처 원칙

- **Admin DB에는 회원·통계 데이터를 저장하지 않는다.** 각 앱의 `/internal/admin/*` API를 실시간으로 호출해 조회한다 (`integration.AppAdminClient` 참고).
- **각 앱의 DB에 직접 접근하지 않는다.** 반드시 앱이 제공하는 admin 전용 API를 통해서만 연동한다.
- 앱 연동 정보(`managed_app`, `app_platform`, `app_connection`)는 Admin DB에 저장하되, API 키 등 비밀값은 DB에 저장하지 않고 `app_connection.credential_env_key` 에 환경변수 이름만 저장한다. 실제 비밀값은 환경변수로만 주입한다.
- 모든 설정값(DB 접속 정보, 포트 등)은 환경변수로 주입한다. 비밀값은 절대 커밋하지 않는다 (`.env` 는 `.gitignore` 대상, `.env.example` 만 커밋).
- 관리자 역할·권한(RBAC), CSV 다운로드는 아직 구현하지 않는다.

## DB 마이그레이션 규칙

- Flyway 사용, 위치는 `admin-api/src/main/resources/db/migration`.
- **이미 적용된(머지되어 배포된) 마이그레이션 파일은 절대 수정하지 않는다.** 스키마를 바꿔야 하면 새 버전 파일(`V{n}__description.sql`)을 추가한다.
- `V1__init_admin_schema.sql`: admin_user, admin_login_history, managed_app, app_platform, app_connection, audit_log.
- `V2__spring_session.sql`: Spring Session JDBC 스키마. `spring-session-jdbc` jar의 `schema-postgresql.sql` 원본을 그대로 복사한 것이므로 직접 수정하지 않는다 (Spring Session 버전을 올리면서 스키마가 바뀌면 새 버전 마이그레이션으로 반영). `spring.session.jdbc.initialize-schema=never` 로 설정되어 있어 Flyway가 스키마를 관리한다.
- `V3__seed_kkori_app.sql`: 첫 관리 대상 앱인 꼬리(`managed_app` + `app_connection`) 시드 데이터. `base_url`/`credential_env_key`는 비밀값이 아니라 연동 위치와 환경변수 "이름"이라 커밋 가능하다.

## 개발 단계

1. **1차**: 로그인, 앱 선택, 회원 조회/관리
2. **2차**: 통계
3. **3차**: 문의, 공지, 푸시
4. **4차**: 광고, 구독

## 현재 진행 상태

- 모노레포 골격 + **1차 단계(로그인 · 앱 선택 · 회원)**, **2차 단계(대시보드/통계) 구현 완료**.
- admin-api
  - **로그인**: `admin_user` 테이블 기반 세션 인증(Spring Session JDBC). 5회 실패 시 30분 계정 잠금(`SecurityConfig.MAX_FAILED_LOGIN_ATTEMPTS`/`LOCK_DURATION`), 모든 시도를 `admin_login_history`에 기록. CSRF는 `CookieCsrfTokenRepository`(쿠키 경로 `/`) + SPA용 `CsrfTokenRequestAttributeHandler` 조합.
  - **앱 선택**: `GET /applications`, `GET /applications/{appCode}` — `managed_app` 조회. 꼬리 앱은 V3 마이그레이션으로 시드.
  - **회원**: `GET/POST /apps/{appCode}/members[...]` — Admin DB에 저장하지 않고 매 요청마다 `AppAdminClientFactory`가 `app_connection` 설정으로 동적 `RestClient`를 만들어 앱의 admin API를 실시간 호출. 모든 조회/정지/강제 로그아웃은 `audit_log`에 성공·실패 모두 기록.
  - `integration.AppAdminClient`의 회원 관련 엔드포인트 계약(`/members`, `/members/{id}`, `/suspend`, `/force-logout`)은 `kkori-api`(`/Users/dayeong/Desktop/projects/kkori-api`) 쪽에 동일한 계약으로 실제 구현되어 curl로 연동 확인까지 마쳤다. 자세한 내용은 kkori-api의 `src/main/java/com/kkori/api/admin/` 참고.
  - **대시보드/통계**: `GET /apps/{appCode}/statistics/{overview|members|pets|records|conversion}` — `MemberController`/`MemberService`와 동일 패턴(`requireConnection` → `withAudit`)으로 `AppAdminClient`의 `getDashboard*` 5개 메서드를 통해 kkori-api `/internal/admin/dashboard/**`를 그대로 프록시한다. Admin DB에는 아무것도 저장하지 않는다. 조회도 `audit_log`에 `STATISTICS_VIEW`(target_type `STATISTICS`, target_id는 탭 이름)로 기록한다. 지표 정의(활성/참여율 분모/리텐션/KST 변환 등)는 kkori-api `AGENTS.md`의 "10-1. 관리자 대시보드(통계) API" 참고 — admin-api는 그 결과를 그대로 중계할 뿐 재계산하지 않는다.
  - `./gradlew build` 성공 (Testcontainers로 실제 Postgres에 V1~V3 적용 + 로그인/잠금/앱 목록/회원·통계 프록시+감사로그 흐름까지 통합 테스트로 검증. `StatisticsControllerTest`가 회원 쪽 `MemberControllerTest`와 동일하게 스텁 HTTP 서버로 kkori-api 응답을 흉내 낸다).
- admin-web
  - react-router-dom 재설치(로그인/회원 페이지가 생기면서 라우팅 필요해짐). `AuthContext`가 `/auth/me`로 세션 확인, `RequireAuth`가 미인증 시 `/login`으로 리다이렉트.
  - `LoginPage`, `AppLayout`(앱 선택기가 `GET /applications` 조회로 대체됨, 사이드바에 대시보드/회원 관리 메뉴 2개), `MembersPage`(검색·페이지네이션·상세 Drawer·정지 Modal·강제 로그아웃 Popconfirm).
  - **`DashboardPage`**(로그인 후 기본 화면, `AppIndexRedirect`가 `/apps/{appCode}/dashboard`로 리다이렉트): 공통 기간 필터(일/주/월 `Segmented` + `<input type="date">` range, dayjs 의존성 없이 네이티브 date input 사용) + `Tabs` 5개(개요/회원/반려동물/기록/전환·리텐션), 탭마다 독립된 `useQuery(['statistics', appCode, tab, unit, from, to])`. 차트는 `recharts`(이미 설치돼 있던 것 사용).
  - `npm run build`, `npm run lint` 통과 — 로컬 Node가 16이라 Vite 8/rolldown 빌드가 안 돼 nvm의 Node 26으로 재설치 후 확인함(`.claude/launch.json`이 Node 26을 강제하는 `run-admin-web-dev.cmd` 래퍼를 씀 — 로컬 전용 스크립트이니 필요 없어지면 정리).
- infra: docker-compose(postgres 16 + admin-api + nginx) 빌드·기동 확인. `.env.example`에 `ADMIN_BOOTSTRAP_*` 추가.
- **실제 앱(꼬리) admin API가 아직 없어** 회원 목록/상세/정지/강제 로그아웃은 로컬에서 502(연동 실패)로 응답하는 것까지만 확인했다. 그 외 로그인/잠금/앱 선택은 docker compose로 기동해 curl로 전체 플로우(로그인 → 세션 쿠키 → CSRF → 잠금 → 앱 목록)를 end-to-end로 검증했다. **대시보드도 마찬가지로 kkori-api 쪽 `/internal/admin/dashboard/**` 자체는 kkori-api 저장소의 Testcontainers 테스트로 검증했지만, admin-web에서 실제 꼬리 앱 데이터로 화면까지 띄워본 적은 없다** — admin-web 빌드/lint와 로그인 화면 렌더링(콘솔 에러 없음)까지만 브라우저로 확인했다.

### 결정 사항

- admin-api의 `server.servlet.context-path` 를 `/admin-api` 로 고정해, Vite dev 프록시와 nginx 프록시 모두 경로를 재작성(rewrite)하지 않고 그대로 전달하도록 통일했다.
- CSRF/세션 쿠키 경로: Spring Session 쿠키는 기본값(컨텍스트 경로 `/admin-api`)을 그대로 쓰지만, CSRF 쿠키(`XSRF-TOKEN`)는 `/`로 넓혀야 한다. SPA가 `/`에서 서빙되는데 컨텍스트 경로로 스코프된 쿠키는 `document.cookie`로 읽을 수 없어서다(`SecurityConfig`).
- `AuthService.login()`은 `@Transactional(noRollbackFor = {BadCredentialsException, LockedException, DisabledException})` 이어야 한다. 이 예외들은 unchecked라 기본 롤백 규칙을 적용하면 로그인 실패 시 남기려는 실패 이력·잠금 카운트 증가까지 통째로 롤백되어 사라진다 — 실제로 이 버그가 있었고(로컬 docker 환경에서 curl로 5연속 실패 후에도 잠기지 않는 것을 발견), 수정 후 `AuthServiceTest`로 회귀 테스트를 추가했다.
- Gradle Kotlin DSL(`build.gradle.kts`) 사용.
- `AppPlatform`(iOS/Android/Web 스토어 정보) 엔티티는 아직 만들지 않았다. 1차 범위(로그인/앱 선택/회원)에 필요하지 않아서다 — 앱 상세 관리 화면이 생기면 추가.
- 감사 로그 action은 `MEMBER_VIEW`처럼 조회 하나에 통계 5개 탭도 `STATISTICS_VIEW` 하나로 통일했다(탭별로 액션을 나누지 않고 `target_id`에 탭 이름을 넣어 구분). 탭마다 다른 action이 필요해지면 이때 갈라도 된다.
- `AppAdminClient`의 `Dashboard*Response` record는 kkori-api `com.kkori.api.admin.dto.response.dashboard.*` record와 필드명이 1:1이어야 한다(둘 다 Jackson 기본 직렬화라 이름이 곧 계약). 한쪽만 필드를 바꾸면 역직렬화가 조용히 null/0으로 빠지므로, 바꿀 때 항상 양쪽을 함께 수정한다.

### 미해결 이슈

- `lastActiveAt`은 kkori-api에 마지막 활동 시각 추적이 없어 항상 null이다.
- `/internal/admin/**`가 kkori-api의 공개 운영 도메인(`api.kkori.co.kr`) 아래 그대로 노출된다. API 키로만 막고 있어 충분히 긴 무작위 키를 쓰고, 가능하면 kkori-api 쪽 Nginx에서 admin-api 발신 IP로 추가 제한하는 걸 권장한다 (kkori-api README "관리자(admin) 연동" 참고).

### 로컬 개발 시 kkori-api와 함께 띄우기

admin-api(`kkutudio-admin/infra`)와 kkori-api(별도 저장소, `/Users/dayeong/Desktop/projects/kkori-api`)는 별도 docker-compose 프로젝트라 기본적으로 서로의 컨테이너를 이름으로 찾지 못한다. 같이 띄우려면:

```bash
docker network create kkori-shared   # 최초 1회
```

- kkori-api `docker-compose.yml`의 `api` 서비스가 `kkori-shared` 네트워크에 `kkori-api`라는 별칭으로 연결되어 있다 (`app_connection.base_url`의 시드값 `http://kkori-api:8080/internal/admin`과 맞춘 것).
- `infra/docker-compose.yml`의 `admin-api` 서비스도 같은 네트워크에 연결된다.
- `infra/.env`의 `KKORI_ADMIN_API_KEY`와 kkori-api `.env`의 `ADMIN_API_KEY`는 같은 값이어야 한다.
- postgres 호스트 포트가 겹치지 않도록 kkutudio-admin 쪽은 `ADMIN_DB_HOST_PORT`(기본 5433)로 분리해뒀다.

두 스택을 `docker compose up -d`로 각각 올리면 admin-web → admin-api → kkori-api 전체 경로가 실제로 연결된다 (curl과 admin-web UI 모두로 확인 완료).

### 운영 배포 시 주의

- `app_connection.base_url`/`credential_env_key`는 [V3__seed_kkori_app.sql](../admin-api/src/main/resources/db/migration/V3__seed_kkori_app.sql)로 시드된 **로컬 개발용 초기값**이다 (`http://kkori-api:8080/internal/admin`). 마이그레이션은 환경별로 값이 달라야 하는 설정을 담는 곳이 아니므로, 이미 적용된 이 파일은 수정하지 않는다. 운영 DB에는 배포 후 한 번 직접 갱신한다:
  ```sql
  UPDATE app_connection
  SET base_url = 'https://api.kkori.co.kr/internal/admin'
  WHERE app_id = (SELECT id FROM managed_app WHERE app_code = 'kkori');
  ```
  `credential_env_key`(`KKORI_ADMIN_API_KEY`)는 이름 그대로 두고, admin-api를 실행하는 운영 환경에 그 이름의 환경변수로 kkori-api의 `ADMIN_API_KEY`와 동일한 값을 넣는다.
- 앱 등록/연동 정보를 UI에서 편집하는 기능은 아직 없다(1차 범위에 없음, `GET`만 구현). 지금은 위 SQL처럼 직접 갱신해야 한다 — 관리 화면이 필요해지면 `ManagedAppController`에 PUT을 추가하는 게 다음 단계.
