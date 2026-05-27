# Requirements

## 범위와 진행 상태

FinRecon은 가상의 외부 결제 거래와 내부 원장을 비교하여 정산 결과를 관리하는 단일 Spring Boot 애플리케이션이다. Phase 0은 실행 기반과 계약 문서를 정의한다. 기능 구현 여부는 아래와 같다.

| Phase | 범위 | 상태 |
| --- | --- | --- |
| 0 | 초기 구성, 데이터 모델, API 초안, 로컬 DB, CI 기준선 | 완료 |
| 1 | 거래 CSV 업로드, 검증, 오류/거래 조회 | 예정 |
| 2 | 원장 업로드, 대사 실행과 요약 조회 | 예정 |
| 3 | Spring Batch 기반 일별 정산 | 예정 |
| 4 | JWT 인증, 권한, 감사 로그 | 예정 |
| 5 | 운영/성능/포트폴리오 보강 | 예정 |

## 핵심 업무 규칙

### 거래

- `transactionId`는 외부 거래를 유일하게 식별하며 중복 저장하지 않는다.
- 거래 유형은 `APPROVAL`, `CANCEL`만 지원한다.
- 승인 금액은 0보다 커야 한다.
- 취소는 `originalTransactionId`가 필요하며 원승인의 취소 가능 잔액을 초과할 수 없다.
- 결제 식별값은 입력과 응답 모두 마스킹된 값만 취급한다.

### 업로드

- 거래와 원장 입력은 CSV만 허용한다.
- 파일 SHA-256 해시를 `uploaded_files.content_hash`에 저장하여 동일 파일 재처리를 막는다.
- 파일 전체를 폐기하지 않고 정상 행은 저장하며 오류 행은 `validation_errors`에 남긴다. 운영자가 오류 데이터만 수정하여 재처리할 수 있고 정상 거래의 처리 지연을 줄이기 위해서다.

### 대사

- `transactionId` 기준으로 외부 거래와 원장을 조인한다.
- 결과 분류는 `MATCHED`, `LEDGER_MISSING`, `TRANSACTION_MISSING`, `AMOUNT_MISMATCH`, `STATUS_MISMATCH`이다.
- 동일 영업일에 다시 실행할 때 기존 결과를 교체하는 정책을 Phase 2 구현 시 트랜잭션과 함께 확정한다.

### 정산

- `MATCHED` 승인 합계에서 `MATCHED` 취소 합계를 차감한다.
- 수수료율 기본값은 2.5%이다.
- 금액은 `BigDecimal`과 DB `decimal(19,2)`를 사용하여 부동소수점 오차를 허용하지 않는다.
- `settlements.business_date` 고유 제약으로 일자별 확정 결과 중복을 방지한다.

### 보안과 감사

- Phase 4에서 `OPERATOR`와 `ADMIN` 역할을 적용한다.
- 업로드, 대사, 정산, 재처리는 감사 로그 대상이다.
- 민감 결제 번호 원문은 저장하지 않는다.
