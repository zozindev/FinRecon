# Requirements

## 목표

FinRecon은 외부 결제 대행사에서 받은 거래 CSV와 내부 원장 데이터를 비교해 검증, 대사, 정산 결과를 관리하는 Spring Boot 백엔드 애플리케이션이다. 포트폴리오 관점에서는 단순 CRUD가 아니라 파일 처리, 금액 정합성, 배치성 작업, 인증/권한, 감사 추적을 보여주는 것을 목표로 한다.

## 구현 범위

| 영역 | 상태 | 내용 |
| --- | --- | --- |
| 프로젝트 기반 | 완료 | Java 21, Spring Boot 3.5, Gradle, PostgreSQL, Flyway, H2 테스트 |
| 거래 업로드 | 완료 | CSV 업로드, SHA-256 파일 중복 방지, 행 단위 검증, 정상/오류 분리 저장 |
| 원장 업로드 | 완료 | CSV 업로드, 원장 참조 ID/거래 ID 중복 검증, 정상/오류 분리 저장 |
| 대사 | 완료 | 거래 ID 기준 비교, 일자별 재실행 시 기존 결과 교체 |
| 정산 | 완료 | MATCHED 승인/취소 금액 집계, 2.5% 수수료, 일자별 정산 저장 |
| 인증/권한 | 완료 | Spring Security 기반 로그인, HMAC-SHA256 JWT, OPERATOR/ADMIN 역할 |
| 감사 | 완료 | 로그인, 업로드, 대사, 정산 감사 로그 저장 및 ADMIN 조회 |
| 테스트 | 완료 | MockMvc 통합 테스트로 핵심 흐름 검증 |

## 거래 검증 규칙

- `transactionId`는 전체 거래에서 유일해야 한다.
- 거래 유형은 `APPROVAL`, `CANCEL`만 허용한다.
- 금액은 `BigDecimal`과 DB `decimal(19,2)`로 처리하며 0보다 커야 한다.
- 취소 거래는 `originalTransactionId`가 필요하다.
- 취소 금액은 원 승인 금액을 초과할 수 없다.
- 결제 번호는 마스킹된 값만 저장한다.
- 정상 행은 `external_transactions`, 오류 행은 `validation_errors`에 저장한다.

## 원장 검증 규칙

- `ledgerReferenceId`는 유일해야 한다.
- `transactionId`는 원장 내에서 유일해야 한다.
- `recordDate`는 `yyyy-MM-dd` 형식이어야 한다.
- 금액은 0보다 커야 한다.
- 상태는 `APPROVED`, `CANCELED`만 허용한다.

## 대사 규칙

- `transactionId`를 기준으로 외부 거래와 내부 원장을 비교한다.
- 결과 유형은 `MATCHED`, `LEDGER_MISSING`, `TRANSACTION_MISSING`, `AMOUNT_MISMATCH`, `STATUS_MISMATCH`다.
- 같은 `businessDate`로 다시 실행하면 기존 결과를 삭제하고 새 결과를 저장한다.
- 실행 이력은 `batch_executions`에 저장한다.

## 정산 규칙

- `MATCHED` 승인 금액 합계에서 `MATCHED` 취소 금액 합계를 차감한다.
- 기본 수수료율은 2.5%다.
- `settlements.business_date`는 유일하며 재실행 시 기존 정산을 교체한다.
- 정산 실행 이력과 감사 로그를 남긴다.

## 보안과 감사

- 업무 API는 Bearer JWT가 필요하다.
- `OPERATOR`는 업로드, 조회, 대사, 정산을 실행할 수 있다.
- `ADMIN`은 모든 업무 API에 더해 감사 로그를 조회할 수 있다.
- 초기 개발 계정 비밀번호는 PBKDF2로 저장한다.
- 운영 환경에서는 초기 계정과 `FINRECON_JWT_SECRET`을 교체해야 한다.
