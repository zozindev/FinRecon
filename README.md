# FinRecon

FinRecon은 외부 결제 거래 CSV와 내부 원장을 검증, 대사, 정산하는 Java/Spring Boot 백엔드 포트폴리오 프로젝트입니다. 실제 금융 또는 고객 데이터는 사용하지 않습니다.

## 현재 구현 상태

핵심 업무 흐름이 API와 테스트로 구현되어 있습니다.

- Java 21 / Spring Boot 3.5.14 / Gradle
- PostgreSQL Docker Compose, Flyway 스키마 및 초기 사용자 데이터
- 거래 CSV 업로드, 행 단위 검증, 정상/오류 분리 저장
- 원장 CSV 업로드 및 중복/형식 검증
- Spring Batch 기반 거래 ID 기준 일자별 대사 실행과 결과 요약
- Spring Batch 기반 MATCHED 거래 일일 정산, 기본 수수료율 2.5%
- Spring Security 기반 JWT 로그인, OPERATOR/ADMIN 권한 검사
- 업로드, 로그인, 대사, 정산 감사 로그 저장 및 ADMIN 조회
- MockMvc + H2 통합 테스트

## 기본 계정

로컬 개발용 초기 계정은 Flyway `V2__seed_app_users.sql`에서 생성됩니다.

| Username | Password | Role |
| --- | --- | --- |
| `admin` | `admin123!` | `ADMIN` |
| `operator` | `operator123!` | `OPERATOR` |

운영 환경에서는 반드시 별도 마이그레이션 또는 관리 절차로 계정과 JWT secret을 교체해야 합니다.

## 주요 API

```http
GET /api/v1/status
POST /api/v1/auth/login
POST /api/v1/transaction-files
GET /api/v1/transaction-files/{fileId}
GET /api/v1/transactions
GET /api/v1/validation-errors
POST /api/v1/ledger-files
GET /api/v1/ledger-entries
POST /api/v1/reconciliations
GET /api/v1/reconciliations?businessDate=2026-06-01
GET /api/v1/reconciliations/summary?businessDate=2026-06-01
POST /api/v1/settlements/daily
GET /api/v1/settlements
GET /api/v1/batch-executions
GET /api/v1/audit-logs
```

`/api/v1/status`, `/api/v1/auth/login`, `/actuator/**`를 제외한 업무 API는 `Authorization: Bearer <token>` 헤더가 필요합니다. 감사 로그 조회는 `ADMIN` 권한만 허용됩니다.

## 로컬 실행

필수 요구사항: JDK 21, Docker Desktop

```powershell
docker compose up -d postgres
.\scripts\gradlew-local.bat bootRun
```

상태 확인:

```http
GET http://localhost:8080/api/v1/status
GET http://localhost:8080/actuator/health
```

테스트:

```powershell
.\scripts\gradlew-local.bat test
```

## 예시 흐름

1. `POST /api/v1/auth/login`으로 `operator` 토큰을 발급받습니다.
2. `samples/transactions.csv`를 `POST /api/v1/transaction-files`에 업로드합니다.
3. `samples/ledger-entries.csv`를 `POST /api/v1/ledger-files`에 업로드합니다.
4. `POST /api/v1/reconciliations`에 `{"businessDate":"2026-06-01"}`을 보내 대사를 실행합니다.
5. `POST /api/v1/settlements/daily`에 같은 일자를 보내 정산을 확정합니다.
6. `admin` 토큰으로 `GET /api/v1/audit-logs`에서 감사 로그를 확인합니다.

## 문서

- [요구사항](docs/requirements.md)
- [ERD](docs/erd.md)
- [API 명세](docs/api-spec.md)
- [아키텍처](docs/architecture.md)
- [의사결정 기록](docs/decisions/0001-phase-0-foundation.md)
- [문제 해결 기록](docs/troubleshooting.md)
- [포트폴리오 요약](docs/portfolio-summary.md)
