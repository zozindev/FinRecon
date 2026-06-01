# Architecture

## 개요

FinRecon은 계층형 Spring Boot 백엔드로 구성한다.

```text
Controller -> Service -> Repository -> Database
```

- Controller: HTTP 요청/응답, Bean Validation, API DTO 변환
- Service: CSV 파싱, 업무 검증, 대사/정산 계산, 감사 로그 기록
- Repository: Spring Data JPA 기반 영속성 접근
- Database: PostgreSQL 운영 스키마, H2 테스트 스키마

## 패키지 구조

```text
com.portfolio.finrecon
  api          REST 컨트롤러
  api.dto      요청/응답 DTO
  auth         JWT, 비밀번호 검증, Security 필터, 요청 보안 컨텍스트
  common       공통 응답, 예외, 상태 API
  config       MVC 설정
  domain       JPA 엔티티와 enum
  repository   Spring Data JPA 리포지토리
  service      업무 서비스
```

## 데이터 흐름

1. 운영자가 로그인해 JWT를 발급받는다.
2. 거래 CSV를 업로드하면 파일 해시로 중복 파일을 차단한다.
3. CSV 각 행을 검증해 정상 거래와 검증 오류를 분리 저장한다.
4. 원장 CSV도 동일하게 업로드하고 검증한다.
5. 업무 일자 기준으로 거래와 원장을 `transactionId`로 비교해 대사 결과를 저장한다.
6. 대사 결과 중 `MATCHED` 거래만 일일 정산 대상이 된다.
7. 대사/정산 실행 이력은 `batch_executions`, 주요 행위는 `audit_logs`에 저장한다.

## 인증 방식

Spring Security의 stateless filter chain에서 HMAC-SHA256 JWT를 검증한다. JWT 서명과 비밀번호 검증은 Java 표준 crypto를 사용하며, 비밀번호는 PBKDF2-SHA256 형식으로 저장한다.

운영 환경에서는 다음 값을 환경 변수로 지정한다.

```text
FINRECON_JWT_SECRET
FINRECON_JWT_EXPIRES_IN_SECONDS
```

## 금액 처리

금액은 Java `BigDecimal`, DB `decimal(19,2)`로 처리한다. 정산 수수료는 `HALF_UP` 반올림으로 소수점 둘째 자리까지 확정한다.
