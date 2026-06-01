# FinRecon

금융 거래 CSV를 검증하고, 내부 원장과 대사한 뒤, 정상 거래 기준으로 일일 정산까지 수행하는 Java/Spring Boot 백엔드 포트폴리오 프로젝트입니다.

실제 금융사, 고객, 계좌, 카드 데이터는 사용하지 않았으며 모든 데이터는 포트폴리오용 샘플입니다.

## 3분 요약

**문제 상황**

외부 결제 파트너가 매일 거래 CSV를 전달하고, 내부 시스템에는 별도 원장 데이터가 쌓입니다. 운영자는 두 데이터가 일치하는지 확인하고, 정상 거래만 기준으로 정산 금액을 계산해야 합니다. 이 과정에서 중복 파일, 잘못된 거래 행, 금액 불일치, 상태 불일치, 감사 추적 누락이 발생할 수 있습니다.

**해결한 흐름**

```text
로그인
  -> 거래 CSV 업로드/검증
  -> 원장 CSV 업로드/검증
  -> Spring Batch 대사 실행
  -> MATCHED 거래 기준 일일 정산
  -> 배치 이력/감사 로그 조회
```

**핵심 구현**

- CSV 업로드 시 SHA-256 해시로 같은 파일 재처리 방지
- 행 단위 검증으로 정상 거래와 오류 행 분리 저장
- `transactionId` 기준 거래-원장 대사 및 결과 분류
- Spring Batch `JobLauncher` 기반 대사/정산 실행과 재시도 API
- `BigDecimal`과 `decimal(19,2)` 기반 금액 계산
- Spring Security + JWT 기반 OPERATOR/ADMIN 권한 제어
- Flyway 기반 업무 테이블 및 Spring Batch 메타 테이블 관리
- MockMvc 통합 테스트와 GitHub Actions CI

## 기술 스택

| 영역 | 기술 |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 3.5.14 |
| API | Spring Web MVC |
| Security | Spring Security, JWT |
| Batch | Spring Batch |
| Persistence | Spring Data JPA, Flyway |
| Database | PostgreSQL, H2 test |
| Test/CI | JUnit 5, MockMvc, GitHub Actions |
| Infra | Docker Compose |

## 주요 API

| Method | Endpoint | 설명 |
| --- | --- | --- |
| `POST` | `/api/v1/auth/login` | JWT 발급 |
| `POST` | `/api/v1/transaction-files` | 거래 CSV 업로드 및 검증 |
| `GET` | `/api/v1/validation-errors` | 검증 오류 조회 |
| `POST` | `/api/v1/ledger-files` | 원장 CSV 업로드 |
| `POST` | `/api/v1/reconciliations` | 일자별 대사 실행 |
| `GET` | `/api/v1/reconciliations/summary` | 대사 결과 요약 |
| `POST` | `/api/v1/settlements/daily` | 일일 정산 실행 |
| `GET` | `/api/v1/batch-executions` | 배치 실행 이력 조회 |
| `POST` | `/api/v1/batch-executions/{id}/retry` | 배치 재시도 |
| `GET` | `/api/v1/audit-logs` | 감사 로그 조회, ADMIN 전용 |

OpenAPI 명세는 애플리케이션 실행 후 `GET /openapi.yaml`에서 확인할 수 있습니다.

## 빠른 실행

필수 요구사항: JDK 21, Docker Desktop

```powershell
docker compose up -d postgres
.\scripts\gradlew-local.bat bootRun
```

상태 확인:

```http
GET http://localhost:8080/api/v1/status
GET http://localhost:8080/actuator/health
GET http://localhost:8080/openapi.yaml
```

테스트:

```powershell
.\scripts\gradlew-local.bat test
```

## 데모 시나리오

### 1. 로그인

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "operator",
  "password": "operator123!"
}
```

### 2. 샘플 CSV 업로드

```http
POST /api/v1/transaction-files
Authorization: Bearer <operator-token>
Content-Type: multipart/form-data

file=samples/transactions.csv
```

```http
POST /api/v1/ledger-files
Authorization: Bearer <operator-token>
Content-Type: multipart/form-data

file=samples/ledger-entries.csv
```

### 3. 대사와 정산 실행

```http
POST /api/v1/reconciliations
Authorization: Bearer <operator-token>
Content-Type: application/json

{
  "businessDate": "2026-06-01"
}
```

```http
POST /api/v1/settlements/daily
Authorization: Bearer <operator-token>
Content-Type: application/json

{
  "businessDate": "2026-06-01"
}
```

샘플 데이터 기준 정산 결과:

| 항목 | 금액 |
| --- | ---: |
| 승인 합계 | 12,000.00 |
| 취소 합계 | 2,000.00 |
| 정산 대상 금액 | 10,000.00 |
| 수수료 2.5% | 250.00 |
| 지급 예정 금액 | 9,750.00 |

## 기본 계정

로컬 개발용 초기 계정은 Flyway 마이그레이션으로 생성됩니다.

| Username | Password | Role |
| --- | --- | --- |
| `operator` | `operator123!` | `OPERATOR` |
| `admin` | `admin123!` | `ADMIN` |

운영 환경에서는 초기 계정과 `FINRECON_JWT_SECRET`을 반드시 교체해야 합니다.

## 설계 의도

- **정상/오류 분리 저장**: 전체 파일 실패가 아니라 오류 행만 별도 관리해 운영자가 수정 대상을 빠르게 찾도록 했습니다.
- **재실행 가능성**: 같은 업무 일자의 대사/정산은 기존 결과를 교체해 중복 확정을 방지합니다.
- **금액 안정성**: 금융성 금액은 `double` 대신 `BigDecimal`과 DB `decimal(19,2)`로 처리합니다.
- **운영 추적성**: 로그인, 업로드, 대사, 정산을 감사 로그로 남기고 배치 실행 이력을 별도 관리합니다.
- **포트폴리오 검증성**: 핵심 업무 흐름을 MockMvc 통합 테스트로 검증하고 CI에서 자동 실행합니다.

## 문서

- [요구사항](docs/requirements.md)
- [API 명세](docs/api-spec.md)
- [OpenAPI 3 명세](src/main/resources/static/openapi.yaml)
- [ERD](docs/erd.md)
- [아키텍처](docs/architecture.md)
- [의사결정 기록](docs/decisions/0001-phase-0-foundation.md)
- [문제 해결 기록](docs/troubleshooting.md)
- [포트폴리오 요약](docs/portfolio-summary.md)
