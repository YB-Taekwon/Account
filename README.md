# 계좌 관리 시스템
**목적**
- 사용자와 계좌의 정보를 저장하고, 외부 시스템에서 거래를 요청할 경우 거래 정보를 받아 결제 및 결제 취소와 같은 거래 관리 기능을 제공한다.
- 계좌 확인, 계좌 생성, 계좌 해지, 결제, 결제 취소, 거래 내역 6가지 API를 제공한다.

<br>

## Requirements
- H2: Memory DB
- Redis: Embedded redis
- Data Type: JSON
- Persistence Layer: JPA

<br>

## Development Environment
- **IDE**: IntelliJ Ultimate
- **Spring Initializr**
  - Spring Boot: 3.4.4
  - Language: Java
  - JDK: 21
  - Build: Gradle
- **Dependencies**
  - Lombok
  - Spring Data JPA
  - H2 Database
- **Redis**: Embedded Redis
