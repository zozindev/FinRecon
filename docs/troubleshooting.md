# Troubleshooting Log

## 2026-05-27: 빈 작업공간과 개발 도구 부재

- 증상: 브리프 파일만 존재했고 `git`, `gh`, `java`, Docker가 경로에 없었다.
- 처리: JDK 21, Git, GitHub CLI를 설치하고 공식 Spring Initializr에서 Spring Boot 3.5.14 Gradle 기준선을 생성했다.
- 처리: Docker Desktop을 설치하고 WSL 2 Windows 기능 활성화를 진행했다.
- 남은 제약: Windows가 WSL 2 적용을 위해 재시작을 요구하므로 현재 세션에서 PostgreSQL 컨테이너 연결 검증은 완료할 수 없다. 자동 테스트는 H2 PostgreSQL 모드와 Flyway 적용을 이용해 통과했다.

## 2026-05-27: 브리프 한글 표시 오류

- 증상: PowerShell 기본 파일 읽기에서 UTF-8 한글이 깨져 보였다.
- 처리: UTF-8 인코딩을 명시하여 브리프 요구사항을 다시 판독했다.

## 2026-05-27: Windows Gradle 테스트 워커 실행 오류

- 증상: 기본 `gradlew.bat test` 실행 중 Gradle 테스트 워커가 `GradleWorkerMain`을 로드하지 못했다.
- 원인 판단: 한글이 포함된 사용자 홈/임시 경로를 사용하는 Windows 실행 환경에서 워커 클래스패스가 손상되는 현상으로 판단했다.
- 처리: `.gradle-user-home`과 `.tmp`를 프로젝트 내부 ASCII 경로로 지정하면 `clean test`가 통과한다. PowerShell 실행 정책에도 영향을 받지 않도록 `scripts/gradlew-local.bat`을 제공한다.
