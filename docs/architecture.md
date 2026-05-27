# Architecture

## 단일 애플리케이션 선택

초기 목표는 데이터 정합성과 배치 흐름을 설명할 수 있는 완성 가능한 서비스다. 배포 단위와 트랜잭션 경계를 단순하게 유지하기 위해 마이크로서비스, 메시징, 별도 UI를 포함하지 않는다.

## 패키지 방향

```text
com.portfolio.finrecon
  common/              공통 응답, 예외, 설정, 상태 API
  transaction/         외부 거래와 검증
  upload/              파일 해시와 CSV 처리
  ledger/              내부 원장
  reconciliation/      비교 결과 산출
  settlement/          일별 정산
  batch/               실행 이력과 Spring Batch 작업
  auth/                인증/권한
  audit/               감사 이벤트
```

## 데이터 흐름

1. 파일 업로드 서비스가 해시 중복을 확인하고 행을 파싱한다.
2. 도메인 검증을 통과한 거래만 저장하고 실패 사유는 별도 테이블에 저장한다.
3. 대사 서비스가 영업일과 거래 ID 기준으로 양쪽 데이터를 비교한다.
4. 정산 배치는 `MATCHED` 결과만 모아 승인, 취소, 수수료와 지급액을 확정한다.
5. 쓰기 작업은 감사 서비스로 행위 이력을 남긴다.

## 트랜잭션 및 재실행 원칙

- 업로드는 한 파일 처리 단위 트랜잭션과 행 오류 분리 저장의 균형을 Phase 1에서 테스트로 결정한다.
- 대사 실행은 동일 기준일 결과를 원자적으로 교체하여 오래된 결과와 혼합되지 않게 한다.
- 정산은 영업일 고유 제약과 배치 실행 이력으로 멱등성을 확보한다.

## 로컬 환경

- 애플리케이션: Java 21, Gradle Wrapper
- 데이터베이스: `compose.yaml`의 PostgreSQL 17
- 스키마: Flyway `V1__baseline_schema.sql`
- 자동 테스트: H2의 PostgreSQL compatibility mode를 사용한 빠른 Phase 0 검증; 도메인 구현 시 Testcontainers를 추가한다.
