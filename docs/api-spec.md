# API Specification

Base URL: `/api/v1`

OpenAPI 3 명세는 애플리케이션 실행 후 `/openapi.yaml`에서 확인할 수 있다.

공통 성공 응답:

```json
{
  "data": {}
}
```

공통 오류 응답:

```json
{
  "timestamp": "2026-06-01T10:00:00",
  "status": 400,
  "code": "INVALID_REQUEST",
  "message": "Request validation failed.",
  "violations": [
    { "field": "businessDate", "message": "must not be null" }
  ]
}
```

## Public

### `GET /status`

서비스 상태를 확인합니다.

### `POST /auth/login`

JWT access token을 발급합니다.

Request:

```json
{
  "username": "operator",
  "password": "operator123!"
}
```

Response:

```json
{
  "data": {
    "tokenType": "Bearer",
    "accessToken": "<jwt>",
    "expiresInSeconds": 3600,
    "username": "operator",
    "role": "OPERATOR"
  }
}
```

## Transaction Files

### `POST /transaction-files`

외부 거래 CSV를 업로드합니다. `multipart/form-data`의 `file` 필드를 사용합니다.

CSV header:

```csv
transactionId,transactionType,originalTransactionId,merchantId,transactionDate,amount,maskedPaymentNumber,status
```

검증 규칙:

- `transactionId`는 유일해야 합니다.
- `transactionType`은 `APPROVAL`, `CANCEL`만 허용합니다.
- 금액은 0보다 커야 합니다.
- 취소 거래는 `originalTransactionId`가 필요하고 원 승인 금액을 초과할 수 없습니다.
- 결제 번호는 마스킹된 값만 저장합니다.

### `GET /transaction-files/{fileId}`

업로드 파일의 처리 통계를 조회합니다.

### `GET /transactions`

저장된 정상 거래 목록을 조회합니다.

### `GET /transactions/{transactionId}`

거래 상세를 조회합니다.

### `GET /validation-errors?fileId={fileId}`

검증 실패 행을 조회합니다. `fileId`는 선택 값입니다.

## Ledger

### `POST /ledger-files`

내부 원장 CSV를 업로드합니다.

CSV header:

```csv
ledgerReferenceId,transactionId,merchantId,recordDate,amount,status
```

### `GET /ledger-entries`

저장된 원장 목록을 조회합니다.

## Reconciliation

### `POST /reconciliations`

일자별 대사를 실행합니다. 같은 일자를 다시 실행하면 기존 결과를 삭제하고 새 결과를 저장합니다.

Request:

```json
{
  "businessDate": "2026-06-01"
}
```

결과 유형:

- `MATCHED`
- `LEDGER_MISSING`
- `TRANSACTION_MISSING`
- `AMOUNT_MISMATCH`
- `STATUS_MISMATCH`

### `GET /reconciliations?businessDate=2026-06-01`

일자별 대사 결과를 조회합니다.

### `GET /reconciliations/summary?businessDate=2026-06-01`

결과 유형별 집계를 조회합니다.

## Settlement

### `POST /settlements/daily`

`MATCHED` 승인 금액에서 `MATCHED` 취소 금액을 차감하고 기본 수수료율 2.5%를 적용해 일일 정산을 확정합니다.

Request:

```json
{
  "businessDate": "2026-06-01"
}
```

### `GET /settlements`

정산 결과 목록을 조회합니다.

## Operations

### `GET /batch-executions`

대사/정산 실행 이력을 조회합니다.

### `POST /batch-executions/{executionId}/retry`

기존 배치 실행 이력의 작업명과 업무 일자를 기준으로 같은 작업을 다시 실행합니다.

### `GET /audit-logs`

감사 로그를 조회합니다. `ADMIN` 권한이 필요합니다.
