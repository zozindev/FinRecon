# ADR 0001: Phase 0 Foundation

- 상태: Accepted
- 날짜: 2026-05-27

## 결정

FinRecon은 Java 21과 Spring Boot 3.5.14 기반 Gradle 단일 애플리케이션으로 시작한다. PostgreSQL 스키마 변경은 Flyway가 관리하고, 로컬 데이터베이스는 Docker Compose로 제공한다.

## 이유

- 브리프가 요구하는 Java/Spring 및 관계형 데이터 처리 역량에 집중할 수 있다.
- 단일 배포 단위는 대사와 정산의 트랜잭션/멱등성 정책을 설명하기 쉽다.
- 마이그레이션을 코드와 버전 관리하면 엔티티 자동 생성보다 스키마 제약의 의도가 명확하다.
- Spring Boot 공식 문서상 2026-05-27 기준 안정 지원되는 3.x 라인인 3.5.14를 사용하여 브리프의 3.x 요구와 최신 보안 수정 흐름을 맞춘다.

## 결과

- JPA 엔티티는 기능 구현 시 테이블 기준선과 맞추어 추가한다.
- Spring Batch와 Spring Security/JWT는 각각 Phase 3, Phase 4에서 기능과 테스트를 함께 추가한다.
- Docker가 없는 환경에서도 기초 테스트는 H2로 수행하며, 도메인 저장 검증부터는 Testcontainers를 사용한다.
