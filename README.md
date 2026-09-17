# kkutudio-admin

여러 앱(첫 대상: 꼬리)을 통합 관리하는 어드민.

프로젝트 개요, 기술 스택, 디렉터리 구조, 실행/빌드 명령, 아키텍처 원칙, 개발 단계는 [AGENTS.md](AGENTS.md) 참고.

## 빠른 시작

```bash
cd infra
cp .env.example .env
docker compose up --build
```

nginx가 http://localhost:8081 에서 admin-web을 서빙하고 `/admin-api/*` 요청을 admin-api로 프록시한다.
