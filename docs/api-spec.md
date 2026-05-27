# API Specification

## 구현된 API

### `GET /api/v1/status`

프로젝트 초기 구성이 동작하는지 확인한다.

```json
{
  "data": {
    "application": "FinRecon",
    "phase": "PHASE_0",
    "status": "READY"
  }
}
```

### 오류 응답 기준선

Bean Validation 실패 응답은 다음 형태를 사용한다.

```json
{
  "timestamp": "2026-05-27T10:00:00Z",
  "status": 400,
  "code": "INVALID_REQUEST",
  "message": "Request validation failed.",
  "violations": [
    { "field": "businessDate", "message": "must not be null" }
  ]
}
```

## 예정 API 계약

| Phase | Method | Endpoint | 목적 |
| --- | --- | --- | --- |
| 1 | POST | `/api/v1/transaction-files` | 거래 CSV 검증 및 저장 |
| 1 | GET | `/api/v1/transaction-files/{fileId}` | 처리 통계 조회 |
| 1 | GET | `/api/v1/transactions` | 거래 목록 조회 |
| 1 | GET | `/api/v1/transactions/{transactionId}` | 거래 상세 조회 |
| 1 | GET | `/api/v1/validation-errors` | 오류 조회 |
| 2 | POST | `/api/v1/ledger-files` | 원장 CSV 저장 |
| 2 | POST | `/api/v1/reconciliations` | 대사 실행 |
| 2 | GET | `/api/v1/reconciliations` | 결과 조회 |
| 2 | GET | `/api/v1/reconciliations/summary` | 분류별 요약 |
| 3 | POST | `/api/v1/settlements/daily` | 일별 정산 실행 |
| 3 | GET | `/api/v1/settlements` | 정산 조회 |
| 3 | GET | `/api/v1/batch-executions` | 배치 실행 조회 |
| 4 | POST | `/api/v1/auth/login` | JWT 발급 |
| 4 | GET | `/api/v1/audit-logs` | 감사 조회 |

세부 요청/응답 DTO와 HTTP 오류 코드는 각 기능 Phase에서 테스트와 함께 확정한다.
