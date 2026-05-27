# FinRecon

금융 거래 검증, 내부 원장 대사, 일별 정산 흐름을 학습하고 설명하기 위한 Spring Boot 백엔드 포트폴리오 프로젝트입니다. 실제 금융 또는 고객 데이터는 사용하지 않습니다.

## 현재 구현 상태

현재 저장소는 **Phase 0 - 설계 및 초기 구성**을 완료한 기준선입니다.

- Java 21 / Spring Boot 3.5.14 / Gradle 프로젝트 구성
- PostgreSQL Docker Compose와 Flyway 초기 스키마
- JPA, Validation, Actuator 기반 실행 구성
- 공통 API 응답 및 오류 응답 골격
- `GET /api/v1/status` 준비 상태 API
- H2 기반 애플리케이션/웹 테스트와 GitHub Actions CI
- Phase 1-4 구현을 위한 ERD, API 및 아키텍처 문서

거래 업로드, 대사, 정산, JWT 인증 기능은 아직 구현되지 않았으며 후속 Phase의 범위입니다.

## 기술 선택

| 구분 | 기술 | 선택 이유 |
| --- | --- | --- |
| Language | Java 21 | LTS와 현대 Java 문법 기반 학습 |
| Framework | Spring Boot 3.5.14 | 브리프가 요구한 Spring Boot 3.x 안정 버전 |
| Persistence | Spring Data JPA, Flyway | 도메인 접근과 명시적인 스키마 이력 분리 |
| Database | PostgreSQL 17 | 정합성 중심 관계형 모델 |
| Validation | Bean Validation | API 경계 입력 검증 |
| Operations | Actuator, Docker Compose | 로컬 상태 확인 및 반복 실행 |
| Test | JUnit 5, MockMvc, H2 | Phase 0의 빠른 자동 검증 |

## 로컬 실행

필수 도구: JDK 21, Docker Desktop(Windows에서 처음 설치한 경우 WSL 2 활성화 후 재시작 필요)

```bash
docker compose up -d postgres
./gradlew bootRun
```

Windows PowerShell에서는 다음 명령을 사용합니다.

```powershell
docker compose up -d postgres
.\scripts\gradlew-local.bat bootRun
```

환경변수를 변경하지 않으면 애플리케이션은 `jdbc:postgresql://localhost:5432/finrecon`에 `finrecon / finrecon`으로 접속합니다. 다른 값을 사용할 때는 `.env.example`을 기준으로 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`를 설정합니다.

상태 확인:

```http
GET http://localhost:8080/api/v1/status
GET http://localhost:8080/actuator/health
```

테스트:

```powershell
.\scripts\gradlew-local.bat test
```

테스트는 H2 PostgreSQL 호환 모드에서 Flyway 초기 마이그레이션까지 실행하므로 Docker 없이도 동작합니다. Windows 배치 래퍼는 한글이 포함된 사용자 홈 경로에서 Gradle 테스트 워커가 실패할 수 있는 문제를 피하기 위해 캐시와 임시 디렉터리를 프로젝트 아래로 지정합니다. Linux/macOS 또는 ASCII 사용자 경로에서는 `./gradlew test`를 바로 사용할 수 있습니다.

Docker 엔진이 준비된 후 PostgreSQL 구성을 확인하려면 `docker compose up -d postgres` 실행 뒤 `docker compose ps`에서 `healthy` 상태를 확인하고 애플리케이션을 시작합니다.

## 예정 업무 흐름

1. 운영자가 외부 거래 CSV를 업로드하면 정상 행과 오류 행을 분리 저장한다.
2. 관리자가 내부 원장을 업로드하고 거래 ID를 기준으로 대사를 수행한다.
3. 정상 대사된 거래만 일별 정산 대상으로 계산한다.
4. 후속 Phase에서 권한별 접근과 감사 로그 조회를 적용한다.

## 문서

- [요구사항](docs/requirements.md)
- [ERD](docs/erd.md)
- [API 명세](docs/api-spec.md)
- [아키텍처](docs/architecture.md)
- [의사결정 기록](docs/decisions/0001-phase-0-foundation.md)
- [문제 해결 기록](docs/troubleshooting.md)
- [포트폴리오 요약](docs/portfolio-summary.md)

## 시간과 금액 정책

- 거래일과 영업일은 `LocalDate`에 대응하며, 운영 시간 기록은 서버 시간 기준 `timestamp`로 시작한다. 인증/운영 Phase에서 UTC 저장 정책을 보강한다.
- 금액 컬럼은 `decimal(19, 2)`로 정의한다. Java 구현에서는 `BigDecimal`을 사용하고 정산 수수료 반올림 정책은 Phase 3에서 확정한다.
