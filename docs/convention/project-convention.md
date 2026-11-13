# Emotive Ecommerce 백엔드 개발 컨벤션

> 이 문서는 DDD(Domain-Driven Design)를 적용한 Java + Spring Boot 기반 백엔드 프로젝트의 아키텍처·코드·API·DB·Git 컨벤션을 정의한다.
> - 대상은 Java 로 작성된 `apps/commerce-api` 와 `modules/*`, `supports/*` 다. Kotlin 으로 작성된 `apps/commerce-streamer`, `apps/pg-simulator`, `modules/kafka` 는 Checkstyle/Spotless 검사 대상이 아니며, 이 문서에서는 구조와 연동 규약만 다룬다.

---

## 목차

1. [기술 스택](#1-기술-스택)
2. [아키텍처](#2-아키텍처)
3. [네이밍 컨벤션](#3-네이밍-컨벤션)
4. [코드 스타일](#4-코드-스타일)
5. [계층별 작성 규칙](#5-계층별-작성-규칙)
6. [API 설계 규칙](#6-api-설계-규칙)
7. [예외 처리](#7-예외-처리)
8. [검증(Validation)](#8-검증validation)
9. [트랜잭션](#9-트랜잭션)
10. [영속성](#10-영속성)
11. [데이터베이스](#11-데이터베이스)
12. [캐시 / 메시징](#12-캐시--메시징)
13. [로깅](#13-로깅)
14. [설정 / 프로파일](#14-설정--프로파일)
15. [테스트](#15-테스트)
16. [Git 컨벤션](#16-git-컨벤션)
17. [코드 리뷰](#17-코드-리뷰)
18. [문서화](#18-문서화)

---

## 1. 기술 스택

| 구분 | 기술 | 버전 |
|---|---|---|
| Language | Java (commerce-api, modules, supports) | 21 |
| Language | Kotlin (commerce-streamer, pg-simulator, modules/kafka) | 2.0.20 |
| Framework | Spring Boot / Spring Cloud | 3.4.4 / 2024.0.1 |
| Build | Gradle (Kotlin DSL), 멀티 모듈 | 8.13 |
| ORM | Spring Data JPA / Hibernate, QueryDSL (jakarta) | Boot BOM |
| DB | MySQL | 8.0 |
| Migration | 없음 (Hibernate `ddl-auto` 로 관리, [11.3](#113-스키마-관리)) | - |
| Cache | Redis (master 1 + read replica 1), Spring Cache | 7.0 |
| Messaging | Kafka (KRaft 단일 브로커), spring-kafka | 3.5.1 |
| 외부 호출 | Spring Cloud OpenFeign, Resilience4j (CircuitBreaker, Retry) | 2.2.0 |
| Test | JUnit 5, AssertJ, Mockito, Testcontainers, Fixture Monkey, Instancio | 1.1.14 (Fixture Monkey) |
| API 문서 | springdoc-openapi | 2.7.0 |
| 정적 분석 | Checkstyle / Spotless (palantir-java-format) | 14.1.0 / 8.10.3 (2.99.0) |
| 커버리지 | JaCoCo (XML 리포트) | - |
| 모니터링 | Actuator, Micrometer (Prometheus, Brave tracing), Grafana | - |
| 인프라 | Docker Compose (로컬), Terraform (AWS dev), k6 (부하 테스트) | - |

- 버전은 `gradle.properties` 한 곳에서 관리한다. 빌드 스크립트에 버전을 직접 쓰지 않는다.
- 의존성 추가 시 PR 본문에 **추가 사유**와 **대안 검토 여부**를 기재한다.

---

## 2. 아키텍처

### 2.1 설계 원칙

1. **도메인 모델이 중심이다.** 규모 보통 이상의 기능은 비즈니스 규칙을 Entity·VO·Domain Service 에 둔다. Application Service 에 `if` 분기와 규칙이 쌓이면 리팩터링 대상이다. 규모 작음의 기능은 서비스 메서드에 흐름대로 둔다. ([2.2](#22-설계-규모와-ddd))
2. **계층형 아키텍처 + 의존성 역전(DIP)** 을 따른다. Repository, 외부 PG, 메시지 발행은 `domain` 에 인터페이스를 두고 `infrastructure` 가 구현한다.
3. **도메인 엔티티에 JPA 어노테이션 사용을 허용한다.** 별도 JPA 엔티티로 분리하지 않는다.
4. 도메인 간 후속 처리는 **Spring 이벤트**(같은 애플리케이션)와 **Kafka 메시지**(다른 애플리케이션)로 분리한다.

### 2.2 설계 규모와 DDD

설계는 DDD 로 한다. 다만 DDD 는 **도메인이 복잡한 만큼만** 쓴다. 작은 기능에 Aggregate, 포트, 이벤트를 다 갖추면 코드는 늘어나는데 지키는 규칙은 늘지 않는다. 그래서 설계를 시작하기 전에 **설계 규모**를 먼저 정하고, 규모에 맞는 도구만 쓴다.

#### 원칙

1. **규모는 요구사항이 정한다.** 기능 이름이나 예상 코드 양이 아니라 요구사항의 상태, 항상 지켜야 할 규칙, 외부 시스템, 다른 도메인과의 관계로 판단한다.
2. **애매하면 낮은 쪽을 고른다.** 올려 잡을 때는 어떤 요구사항이 그 구조를 필요로 하는지 설계 문서에 쓴다.
3. **규모보다 높은 신호는 그 부분만 보강한다.** 예: 작은 기능에 "동시에 담아도 하나만" 규칙이 있으면 규모는 작음으로 두고, 그 규칙만 유니크 제약으로 지킨다. 외부 연동이 하나 있으면 그 연동에만 인터페이스와 어댑터를 둔다.
4. **기존 프로젝트 구조가 먼저다.** 패키지 구조, 레이어 이름, 상태 변경 방식(예: 조건부 UPDATE)이 이미 있으면 따른다. 기능 하나를 위해 프로젝트 구조를 바꾸지 않는다. DDD 개념(Aggregate 경계, Value Object, 불변식의 위치)은 그 구조 안에서 적용한다.
5. **규모는 기능마다 다르다.** 같은 시스템 안에서도 주문은 큼, 공지사항은 작음일 수 있다.

이 프로젝트에서 **규모와 관계없이 지키는 구조**(원칙 4):

- 패키지 구조([2.5](#25-패키지-구조))와 계층 의존 규칙([2.7](#27-의존-규칙))
- Repository 는 domain 인터페이스 + `{Entity}RepositoryImpl` + `{Entity}JpaRepository` 로 둔다. ([2.8](#28-전술적-설계-규칙))
- 엔티티는 `BaseEntity` 상속, `private` 생성자 + `create(...)` 로 만든다.
- Application Service 는 도메인당 `{Domain}Service` 하나이고, 트랜잭션 경계는 여기서 정한다. ([5.2](#52-application-service-application))

#### 규모 판단

요구사항을 읽고 신호마다 해당하는 칸을 고른다. **가장 많이 해당하는 칸**이 규모다. 단, 큼 칸에 둘 이상 해당하면 큼이다. 수가 같으면 낮은 쪽이다.

| 신호 | 작음 | 보통 | 큼 |
|---|---|---|---|
| 상태 | 없거나 단순한 값 변경 | 한 대상에 여러 상태와 전이 | 여러 대상의 상태가 서로 맞물림 |
| 항상 지켜야 할 규칙 | 규칙마다 한 곳(제약 · 조건 하나)에서 지킴 | 한 대상의 여러 값 · 상태를 함께 지켜야 함 | 여러 대상에 걸쳐 지킴 |
| 외부 시스템 | 없거나 조회만 | 요청 1~2종 | 결과가 불확실한 요청(결과 불명, 대사) |
| 다른 도메인 | 관계 없음 | 다른 도메인 정보를 읽음 | 다른 도메인의 상태를 바꾸거나 그 변화에 반응 |
| 응답 뒤 처리 | 없음 | 없음 | 배치, 이벤트, 따라 바뀌는 것 |
| 동시에 겹치는 요청 | 상관없음 | 같은 대상 갱신을 막는 정도 | "겹쳐도 하나만 반영" 같은 규칙이 있음 |

설계 문서에는 고른 규모와, 신호마다 고른 칸과 근거를 남긴다.

#### 규모별로 쓰는 것

| 항목 | 작음 | 보통 | 큼 |
|---|---|---|---|
| 도메인 규칙의 위치 | 서비스 메서드 하나에 흐름대로 (Transaction Script). 엔티티에는 간단한 메서드만 | **Aggregate Root 메서드**가 불변식과 상태 전이를 지킨다. Application Service 는 순서만 조율한다 | 보통과 같고, 여러 Aggregate 의 협력 방식을 정한다 (아래) |
| Aggregate | 정의하지 않음 | 1~2개 | 필요한 만큼. Aggregate 사이는 ID 로 참조 |
| Value Object | 규칙이 있는 값만 (예: 금액 한도). 없으면 기본 타입 | 규칙이나 계산이 있는 값 | 보통과 같음 |
| Domain Service | 쓰지 않음 | 여러 Aggregate 나 외부 정보를 함께 써야 하는 계산이 있을 때만 | 보통과 같음 |
| Repository | 프로젝트 관례대로 | Aggregate 마다 하나 | 보통과 같음 |
| 외부 시스템 | 기존 클라이언트를 직접 호출. 테스트에서 대체해야 하면 인터페이스 | 인터페이스(포트) + 어댑터 ([2.10](#210-외부-시스템-연동)) | 포트 + 어댑터. 외부 모델을 도메인 모델로 옮기는 번역 계층(Anti-Corruption Layer) |
| 다른 도메인 | 직접 조회 | 조회용 인터페이스 | 도메인 사이의 관계(읽음 · 바꿈 · 반응함)와 연동 방식을 설계 문서에 정하고 따른다 |
| Domain Event | 쓰지 않음 | 쓰지 않음 | 응답 뒤에 따라 바뀌는 것이 있을 때 ([2.9](#29-이벤트와-메시지)) |
| 컴포넌트 수 | 1~3개 | 3~7개 | 제한 없음. 단, 분리 조건을 지킴 |

##### 여러 Aggregate 가 함께 바뀔 때 (큼)

- 요구사항이 "함께 바뀐다", "한쪽만 바뀐 상태가 보이지 않는다"를 요구하면, Application Service 가 **한 트랜잭션에서** 두 Aggregate 를 바꾼다. 갱신 순서를 정하고, 그 근거가 되는 요구사항을 트랜잭션 경계 설명에 쓴다.
- 응답 뒤에 따라 바뀌어도 되면 Domain Event 로 나눈다. 이때 누락을 허용하는지, 여러 번 받아도 한 번만 반영되는지를 정한다.
- 한 트랜잭션에 Aggregate 하나가 DDD 의 기본이다. 이를 벗어나는 경우는 근거가 되는 요구사항과 함께 쓴다.

#### 컴포넌트 나누기

컴포넌트는 **바뀌는 이유 하나에 하나**다. 요구사항이 많다고 나누지 않고, 계층이 있어야 할 것 같다고 나누지 않는다.

다음 중 하나에 해당할 때만 따로 둔다. 설계 문서에는 컴포넌트마다 해당하는 조건 번호를 적는다.

1. 트랜잭션 경계가 다르다 (예: 외부 호출을 트랜잭션 밖에 둬야 함)
2. 외부 시스템이나 다른 도메인과 연동한다
3. 둘 이상의 흐름이 같은 규칙을 함께 쓴다
4. Aggregate · Value Object 로 불변식을 지킨다
5. 진입점이 따로 있다 (요청, 배치, 이벤트 처리)

다음은 만들지 않는다.

- 한 흐름에서만 쓰는 검증 몇 줄을 위한 Validator 클래스. 검증은 Aggregate 나 서비스 메서드 안에 둔다. (여러 도메인이 함께 쓰는 `support/validation` 의 공통 유틸은 조건 3 에 해당한다.)
- 구현이 하나뿐이고 테스트에서 대체하지 않는 내부 클래스의 인터페이스. (Repository 인터페이스는 프로젝트 관례이므로 예외다.)
- 호출을 그대로 넘기기만 하는 계층 (Facade, Manager, Helper)
- 조회만 하는 흐름을 위해 Aggregate 를 거치는 경로. 조회는 조회용 쿼리로 바로 읽어도 된다.
- 규모 표에서 "쓰지 않음"인 도구

### 2.3 모듈 구성

```
Root (loopers-java-spring-template)
├── apps                      # 실행 가능한 SpringBootApplication
│   ├── commerce-api          # 커머스 API (Java)          : 8080 / actuator 8081
│   ├── commerce-streamer     # Kafka 컨슈머·집계 (Kotlin)  : 8085
│   └── pg-simulator          # PG 시뮬레이터 (Kotlin)      : 8082 / actuator 8083
├── modules                   # 도메인에 의존하지 않는 재사용 설정
│   ├── jpa                   # DataSource, JPA, QueryDSL 설정, BaseEntity, jpa.yml
│   ├── redis                 # master/replica RedisTemplate, @EnableCaching, redis.yml
│   └── kafka                 # Producer/Consumer/BatchListener 설정, kafka.yml
└── supports                  # 부가 기능 add-on
    ├── jackson               # ObjectMapper 커스터마이징
    ├── logging               # logback, Slack appender, logging.yml
    └── monitoring            # actuator, prometheus, monitoring.yml
```

- `apps` → `modules`, `supports` 방향으로만 의존한다. `modules`, `supports` 는 `apps` 와 특정 도메인을 알지 못한다.
- `apps` 의 모듈만 `bootJar` 를 만들고, 나머지는 일반 `jar` 다. (루트 `build.gradle.kts`)
- 모듈별 설정 파일(`jpa.yml` 등)은 모듈이 소유하고, 앱의 `application.yml` 이 `spring.config.import` 로 가져온다.

### 2.4 도메인 구성 (commerce-api)

| 도메인 (패키지) | 책임 | 주요 엔티티 | 테이블 |
|---|---|---|---|
| 회원 (`user`) | 가입, 내 정보 조회 | `User` | `member` |
| 브랜드 (`brand`) | 브랜드 목록·상세 조회 | `Brand` | `brand` |
| 상품 (`product`) | 상품 목록·상세 조회, 재고 | `Product`, `ProductStock` | `product`, `product_stock` |
| 좋아요 (`productlike`) | 상품 좋아요 등록·취소, 좋아요 수 | `ProductLike`, `ProductLikeCount` | `product_like`, `product_like_count` |
| 주문 (`order`) | 주문 생성·완료·취소, 주문 상태 | `Order`, `OrderItem` | `orders`, `order_item` |
| 결제 (`payment`) | PG 결제 요청, 콜백 처리, 상태 복구 | `Payment` | `payment` |
| 포인트 (`point`) | 포인트 충전·사용·복원 | `Point` | `point` |
| 쿠폰 (`coupon`) | 쿠폰 할인 계산, 사용·복원 | `Coupon` | `coupon` |

### 2.5 패키지 구조

패키지는 **계층 우선(layer-first)** 으로 나누고, 계층 아래에 도메인 패키지를 둔다.

```
com.loopers
├── CommerceApiApplication.java
├── interfaces
│   └── api
│       ├── ApiResponse.java              # 공통 응답 래퍼
│       ├── ApiControllerAdvice.java      # 전역 예외 처리
│       └── {domain}
│           ├── {Domain}Controller.java
│           ├── {Domain}ApiSpec.java      # springdoc 어노테이션 전용 인터페이스
│           └── dto                       # *Request, *Response
├── application
│   └── {domain}
│       ├── {Domain}Service.java          # 유스케이스, 트랜잭션 경계
│       ├── dto                           # *Command, *Result, *Condition
│       └── event
│           ├── {Domain}EventListener.java
│           └── model                     # *Event (과거형)
├── domain
│   └── {domain}
│       ├── {Entity}.java, {Enum}.java
│       ├── {Entity}Repository.java       # 인터페이스
│       ├── {DomainService}.java          # OrderProcessing, CouponRedemption ...
│       ├── dto                           # *CreateInfo, dto/command, dto/result
│       ├── vo                            # 값 객체, 일급 컬렉션
│       └── message
│           ├── {Domain}MessagePublisher.java   # Kafka 발행 포트
│           └── model                           # *Message, *MessageEnvelope, *MessageType
├── infrastructure
│   ├── {domain}
│   │   ├── jpa                           # {Entity}JpaRepository, {Entity}RepositoryImpl
│   │   ├── kafka                         # Kafka{Domain}MessagePublisher
│   │   ├── pgclient                      # 외부 PG 연동 (Feign, config, dto)
│   │   └── scheduler
│   └── external
│       └── {system}                      # 도메인에 속하지 않는 외부 시스템 연동
└── support                               # 앱 공통 (도메인 무관)
    ├── config                            # WebConfig, AsyncContextPropagationConfig
    ├── error                             # CoreException, ErrorType
    ├── filter, interceptor, resolver     # X-USER-ID 처리
    ├── converter, validation, dto
```

- 패키지명은 소문자 한 단어로 쓴다. 두 단어 도메인은 붙여 쓴다: `productlike` (O), `product_like` (X)
- `modules/jpa` 의 `JpaConfig` 는 `com.loopers.infrastructure`, `com.loopers.domain.*` 에서만 Spring Data Repository 를 스캔한다. JpaRepository 를 다른 패키지에 두면 빈으로 등록되지 않는다.
- `support` 에는 특정 도메인의 개념을 두지 않는다.

### 2.6 계층별 책임

| 계층 | 책임 | 포함 요소 | 금지 |
|---|---|---|---|
| interfaces | HTTP 요청/응답 변환, 요청 형식 검증 | Controller, ApiSpec, Request, Response | 비즈니스 로직·비즈니스 검증, Repository 직접 접근 |
| application | 유스케이스 흐름 조율, 트랜잭션, 이벤트 발행·처리, 존재·소유자·여러 애그리거트에 걸친 정책 검증 | Service, Command, Result, Event, EventListener | 비즈니스 규칙 구현, interfaces DTO 참조, HTTP 개념 |
| domain | 비즈니스 규칙, 불변식, 상태 변경 | Entity, VO, Domain Service, Repository·Publisher·Client 인터페이스 | application/infrastructure/interfaces import, Web·Kafka·Feign 의존 |
| infrastructure | 기술 구현 | RepositoryImpl, JpaRepository, Kafka Publisher, Feign Client, Scheduler | 비즈니스 규칙 |

### 2.7 의존 규칙

```
interfaces → application → domain ← infrastructure
```

- 의존은 항상 **바깥 → 안쪽(domain)** 방향이다. domain 은 다른 계층에 의존하지 않는다.
- infrastructure 는 domain 에 정의된 인터페이스(`OrderRepository`, `PgClient`, `OrderMessagePublisher`)를 구현한다.
- application 은 구현체(`OrderRepositoryImpl`, `OrderJpaRepository`)가 아닌 domain 인터페이스에만 의존한다.
- 다른 도메인의 상태를 바꿔야 하면 그 도메인의 Service 를 직접 호출하지 않고 **이벤트**를 발행한다. ([2.9](#29-이벤트와-메시지)) 단, 요구사항이 "함께 바뀐다"를 요구하면 [2.2](#22-설계-규모와-ddd) 의 "여러 Aggregate 가 함께 바뀔 때"를 따른다.
- 이 규칙은 Checkstyle `ImportControl` (`config/checkstyle/import-control.xml`) 이 검사한다. 부록 A 의 기존 위반은 파일 단위 허용 목록으로 관리하며, 새 위반을 허용 목록에 추가하지 않는다.

### 2.8 전술적 설계 규칙

#### Entity

- 모든 엔티티는 `modules/jpa` 의 `BaseEntity` 를 상속한다. `BaseEntity` 가 `id`(IDENTITY), `createdAt`, `updatedAt`, `deletedAt` 을 관리한다.
- 생성은 **`private` 생성자 + 정적 팩토리 메서드 `create(...)`** 로 한다. 검증은 팩토리/생성자에서 수행한다.
- 상태 변경은 의미 있는 도메인 메서드로만 한다: `order.cancel()`, `point.charge(amount)`, `coupon.use()`
- 생성 파라미터가 많으면 `domain/{domain}/dto` 의 `record` 로 묶어 전달한다: `OrderCreateInfo`, `ProductCreateCommand`

#### Value Object / 일급 컬렉션

- `domain/{domain}/vo` 에 `record` 로 작성한다: `Products`(상품 일급 컬렉션), `PgRequestResult`
- 불변으로 유지하고, 값과 관련된 판단 로직을 VO 에 둔다. (`PgRequestResult.checkFailed()`)

#### Domain Service

- 한 엔티티에 속하지 않고 **여러 엔티티·Repository 에 걸친 도메인 로직**에 사용한다.
- 이름은 `Service` 접미사 대신 **도메인 용어(행위 명사)** 로 짓는다: `OrderProcessing`, `ProductStockAllocation`, `CouponRedemption`, `PaymentReconciliation`
- `domain/{domain}` 패키지에 두고, domain 의 Repository 인터페이스에만 의존한다.

| 구분 | Application Service | Domain Service |
|---|---|---|
| 위치 | `application/{domain}` | `domain/{domain}` |
| 이름 | `{Domain}Service` | 도메인 용어 (`OrderProcessing`) |
| 역할 | 유스케이스 흐름: 조회 → 도메인 호출 → 이벤트 발행 | 도메인 규칙 계산·판단 |
| 트랜잭션 | 관리함 | 관여하지 않음 |

#### Repository

- 인터페이스는 `domain/{domain}` 에, 구현은 `infrastructure/{domain}/jpa` 에 둔다.
- 구현은 **`{Entity}RepositoryImpl`(`@Component`) + `{Entity}JpaRepository`(Spring Data)** 두 개로 구성한다.
- 단순 CRUD 는 JpaRepository 에 위임하고, 조건·정렬·조인이 있는 조회는 RepositoryImpl 에서 QueryDSL 로 작성한다.
- 인터페이스에 쿼리 계약(집계 방식, 기간 경계, null 반환 여부)이 있으면 Javadoc 으로 남긴다. (`OrderRepository.countCompletedOrdersGroupByUser` 참고)

```java
// domain/order
public interface OrderRepository {
    Order save(Order order);

    Optional<Order> findById(Long id);

    List<Order> findAllByUserId(Long userId);
}

// infrastructure/order/jpa
public interface OrderJpaRepository extends JpaRepository<Order, Long> {}

@Component
public class OrderRepositoryImpl implements OrderRepository {
    private final OrderJpaRepository orderJpaRepository;
    private final JPAQueryFactory queryFactory;

    public OrderRepositoryImpl(OrderJpaRepository orderJpaRepository, EntityManager em) {
        this.orderJpaRepository = orderJpaRepository;
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<Order> findAllByUserId(Long userId) {
        return queryFactory
                .select(QOrder.order)
                .from(QOrder.order)
                .where(QOrder.order.userId.eq(userId))
                .orderBy(QOrder.order.createdAt.desc())
                .fetch();
    }
}
```

#### 식별자(ID) 전략

- 모든 엔티티의 PK 는 **DB IDENTITY (`Long`)** 다. `BaseEntity` 가 선언하므로 엔티티에 `@Id` 를 다시 쓰지 않는다.
- 사용자 식별은 `member.id`(PK)를 그대로 쓴다. 요청 헤더 `X-USER-ID` 값도 이 PK 다. ([6.6](#66-사용자-식별))
- 외부 시스템에 노출하는 식별자는 별도로 생성한다: PG 주문번호 `PgIdGenerator.generatePgOrderId()` → `ORD_{epochMillis}`

### 2.9 이벤트와 메시지

| 구분 | 애플리케이션 이벤트 | Kafka 메시지 |
|---|---|---|
| 범위 | commerce-api 내부, 도메인 간 후속 처리 | commerce-api → commerce-streamer |
| 전달 | `ApplicationEventPublisher` (in-process) | `KafkaTemplate` |
| 네이밍 | `{Domain}{과거분사}Event` : `OrderCreatedEvent` | `{Domain}{과거분사}Message` : `OrderCompletedMessage` |
| 모델 위치 | `application/{domain}/event/model` | `domain/{domain}/message/model` |
| 처리 위치 | `application/{domain}/event/{Domain}EventListener` | `infrastructure/{domain}/kafka/Kafka{Domain}MessagePublisher` |

- 이벤트는 **Application Service 가 상태 변경을 마친 뒤** `applicationEventPublisher.publishEvent(...)` 로 발행한다.
- 이벤트는 불변 `record` 이며, 구독자가 필요한 ID 와 값만 담는다.
- 이벤트를 **구독하는 도메인**의 `application/{domain}/event` 에 리스너를 둔다. (`OrderCreatedEvent` → `PaymentEventListener`, `CouponEventListener`)
- 리스너는 `@TransactionalEventListener(phase = AFTER_COMMIT)` 을 기본으로 하고, DB 를 변경하면 `@Transactional(propagation = REQUIRES_NEW)` 를 함께 선언한다.
- 이벤트를 그대로 Kafka 로 내보내지 않는다. 리스너에서 `event.toXxxMessage()` 로 변환한 뒤 `{Domain}MessagePublisher` 포트를 호출한다.

현재 이벤트 흐름:

```
주문 생성   OrderCreatedEvent    → PaymentEventListener (결제 생성)
                                 → CouponEventListener (쿠폰 사용)
                                 → DataPlatformEventPublisher (데이터 플랫폼 전송)
결제 결과   PaymentResultEvent   → OrderEventListener (주문 완료 / 취소+재고·포인트 복원)
주문 완료   OrderCompletedEvent  → OrderEventListener → Kafka order.v1
주문 취소   OrderCanceledEvent   → CouponEventListener (쿠폰 복원)
좋아요      ProductLikedEvent    → ProductEventListener (좋아요 수 증가) → Kafka product.v1
```

### 2.10 외부 시스템 연동

- 도메인이 원하는 형태의 인터페이스를 `domain` 에 정의하고, `infrastructure` 에서 외부 모델을 번역한다.
- 외부 요청/응답 DTO 는 `infrastructure/{domain}/{client}/dto` 밖으로 나가지 않는다.
- 외부 호출에는 **timeout + CircuitBreaker + Retry + fallback** 을 적용한다. ([14.3](#143-외부-호출-설정))

```java
// domain/payment
public interface PgClient {
    PgRequestResult requestPayment(PgRequest request);

    PgTransactionInfoResult getTransaction(String transactionKey);
}

// infrastructure/payment/pgclient
@Slf4j
@RequiredArgsConstructor
@Component
public class PgSimulatorClient implements PgClient {

    private final PgSimulatorFeignClient pgSimulatorFeignClient;

    @CircuitBreaker(name = "pgRequestPaymentConfig", fallbackMethod = "requestPaymentFallback")
    @Retry(name = "pgRequestPaymentRetryConfig", fallbackMethod = "requestPaymentFallback")
    @Override
    public PgRequestResult requestPayment(PgRequest request) {
        return PgRequestResult.from(pgSimulatorFeignClient.requestPayment(request.toSimulatorRequest()));
    }

    public PgRequestResult requestPaymentFallback(PgRequest request, Throwable throwable) {
        log.error("결제 요청 실패: {}", throwable.getMessage());
        return new PgRequestResult("", PaymentResultStatus.FAILED, "결제 요청 실패");
    }
}
```

---

## 3. 네이밍 컨벤션

### 3.1 공통

| 대상 | 규칙 | 예시 |
|---|---|---|
| 패키지 | 소문자, 단수형, 언더스코어 금지 | `domain.productlike` |
| 클래스 / 인터페이스 | UpperCamelCase, 명사 | `OrderService` |
| 메서드 | lowerCamelCase, 동사로 시작 | `getOrder()` |
| 변수 | lowerCamelCase | `orderItems` |
| 상수 | UPPER_SNAKE_CASE | `MIN_CHARGE_AMOUNT` |
| Enum 값 | UPPER_SNAKE_CASE | `FIXED_AMOUNT` |
| Boolean | `is`, `has`, `can` 접두사 | `isUsed` |
| 컬렉션 | 복수형 | `orderItems`, `products` |

- 약어는 일반 단어처럼 취급한다: `PgClient` (O), `PGClient` (X), `userId` (O), `userID` (X)
- 의미 없는 축약 금지: `ord`, `mbr`, `tmp`, `data`, `info` 단독 사용 지양
- 위 규칙은 Checkstyle 이 검사한다. (`PackageName`, `AbbreviationAsWordInName`, `enumConstantName`, `booleanPrefix`)

### 3.2 클래스 접미사

| 역할 | 접미사 | 예시 | 위치 |
|---|---|---|---|
| Controller | `Controller` | `OrderController` | `interfaces/api/{domain}` |
| API 문서 인터페이스 | `ApiSpec` | `OrderApiSpec` | `interfaces/api/{domain}` |
| 요청 DTO | `Request` | `OrderCreateRequest` | `interfaces/api/{domain}/dto` |
| 응답 DTO | `Response` | `OrderDetailResponse` | `interfaces/api/{domain}/dto` |
| Application Service | `Service` | `OrderService` | `application/{domain}` |
| 유스케이스 입력 | `Command` | `OrderCreateCommand` | `application/{domain}/dto` |
| 유스케이스 출력 | `Result` | `OrderDetailResult` | `application/{domain}/dto` |
| 조회 조건 | `Condition` | `ProductsCondition` | `application/{domain}/dto` |
| 애플리케이션 이벤트 | `Event` (과거형) | `OrderCreatedEvent` | `application/{domain}/event/model` |
| 이벤트 리스너 | `EventListener` | `OrderEventListener` | `application/{domain}/event` |
| Entity / VO | 도메인 용어 (접미사 없음) | `Order`, `OrderItem`, `Products` | `domain/{domain}` |
| Domain Service | 도메인 용어 (행위 명사) | `OrderProcessing`, `CouponRedemption` | `domain/{domain}` |
| 엔티티 생성 입력 | `CreateInfo` / `CreateCommand` | `OrderCreateInfo` | `domain/{domain}/dto` |
| 조회 프로젝션 | `Result` | `UserOrderCountResult` | `domain/{domain}/dto/result` |
| Repository 인터페이스 | `Repository` | `OrderRepository` | `domain/{domain}` |
| Repository 구현 | `RepositoryImpl` | `OrderRepositoryImpl` | `infrastructure/{domain}/jpa` |
| Spring Data Repository | `JpaRepository` | `OrderJpaRepository` | `infrastructure/{domain}/jpa` |
| 메시지 발행 포트 | `MessagePublisher` | `OrderMessagePublisher` | `domain/{domain}/message` |
| 메시지 발행 구현 | `Kafka` + `MessagePublisher` | `KafkaOrderMessagePublisher` | `infrastructure/{domain}/kafka` |
| Kafka 메시지 | `Message` (과거형), `MessageEnvelope` | `OrderCompletedMessage` | `domain/{domain}/message/model` |
| 외부 연동 포트 / 구현 | `Client` | `PgClient`, `PgSimulatorClient` | `domain` / `infrastructure` |
| Feign 인터페이스 | `FeignClient` | `PgSimulatorFeignClient` | `infrastructure/{domain}/{client}` |
| 스케줄러 | `Scheduler` | `PaymentRecoveryScheduler` | `infrastructure/{domain}/scheduler` |
| 설정 | `Config` | `WebConfig`, `FeignClientConfig` | `support/config` 또는 사용처 |
| 설정 바인딩 | `Properties` | `RedisProperties` | 설정 클래스와 같은 패키지 |
| 예외 | `Exception` | `CoreException` | `support/error` |

- `Controller`, `Config`, `JpaRepository`, `Exception` 접미사는 Checkstyle 이 검사한다.
- 목록 조회용 DTO 는 복수형으로 구분한다: `ProductResult`(단건) / `ProductsResult`(목록의 한 행), `OrdersResponse`

### 3.3 메서드 네이밍

| 동작 | Controller | Service | Repository |
|---|---|---|---|
| 단건 조회 | `get`, `getOrder` | `getOrder` (없으면 예외) | `findById`, `findByXxx` (`Optional`) |
| 목록 조회 | `getAll`, `getOrders` | `getAll`, `getOrders` | `findAll`, `findAllByXxx` |
| 존재 여부 | - | - | `existsById`, `existsByXxx` |
| 생성 | `create` | `create` / 도메인 동사 (`order`, `charge`) | `save`, `saveAll` |
| 상태 변경 | 도메인 동사 (`like`, `charge`) | 도메인 동사 (`completeOrder`, `likeProduct`) | - |
| 삭제 | `unlike` 등 도메인 동사 | 도메인 동사 (`unlikeProduct`) | `delete` |
| 집계 | - | - | `countXxxGroupByYyy` |

- `get` = 반드시 존재(없으면 예외), `find` = 없을 수 있음(`Optional` / 빈 리스트) 으로 의미를 구분한다.
- 이벤트 리스너 메서드는 `handle{무엇}` 으로 짓는다: `handlePaymentResult`, `handleUseCoupon`
- 메시지 발행 메서드는 `publish{무엇}` 으로 짓는다: `publishOrderCompleted`
- DTO 변환 메서드는 `toCommand()`, `toCondition()`, `toEntity()`, `from(...)` 을 쓴다.
- 락을 거는 조회는 이름에 드러낸다: `findByProductIdsWithStockLock`

---

## 4. 코드 스타일

### 4.1 포맷

- 포맷은 **Spotless + palantir-java-format** 이 담당한다. 손으로 맞추지 않고 `./gradlew spotlessApply` 를 실행한다.
- 들여쓰기: **스페이스 4칸** (탭 금지)
- 한 줄 최대 길이: **120자** (`package`, `import` 줄 제외)
- 파일 끝 개행 필수, 줄 끝 공백 금지, 줄바꿈 문자는 LF, 인코딩은 UTF-8 (`.editorconfig`)
- 중괄호는 K&R 스타일, 한 줄 `if` 문도 중괄호 생략 금지
- 포매터가 고치지 못하는 네이밍·구조·금지 패턴은 **Checkstyle** 이 검사한다.

| 파일 | 역할 |
|---|---|
| `config/checkstyle/checkstyle.xml` | 검사 규칙. 메시지 끝의 `(3.2)` 는 이 문서의 장 번호, `[id]` 는 규칙 id |
| `config/checkstyle/import-control.xml` | 계층 의존 규칙 ([2.7](#27-의존-규칙)) |
| `config/checkstyle/suppressions.xml` | main / test 차이, 특정 패키지에만 적용하는 규칙의 경로 설정 |

- `error` 는 빌드를 실패시키고, `warning` 은 리포트(`build/reports/checkstyle/`)에만 남는다. 권장 사항과 부록 A 의 기존 위반이 남은 규칙이 `warning` 이다.

```shell
./gradlew spotlessApply                    # 포맷 자동 적용
./gradlew spotlessCheck                    # 포맷 검사
./gradlew checkstyleMain checkstyleTest    # 컨벤션 검사 (error 는 빌드 실패, warning 은 리포트만)
```

- 불가피하게 규칙을 어겨야 하면 `@SuppressWarnings("checkstyle:{id}")` 로 범위를 좁혀 억제하고 **사유를 주석으로 남긴다.**

```java
// 복구 배치는 건별 실패 원인(스택트레이스)을 남겨야 하므로 반복문 내 ERROR 로그를 허용한다.
@SuppressWarnings("checkstyle:logInLoop")
public void recoverPendingPayments() { ... }
```

### 4.2 import

- 와일드카드 import(`*`) 금지
- static import 는 **테스트 코드에서만** 허용
- 사용하지 않는 import 금지 (Spotless 가 제거)
- 순서: `static` → `java` → `javax|jakarta` → `org` → `com` → 기타 (`io`, `lombok` 등), 그룹 사이에 빈 줄

### 4.3 클래스 내부 선언 순서

1. 상수 (`static final`)
2. 필드
3. 생성자
4. 정적 팩토리 메서드
5. public 메서드
6. private 메서드
7. `equals` / `hashCode` / `toString`

- 클래스 어노테이션은 Lombok → Spring/JPA 순으로 쓴다: `@Getter` `@NoArgsConstructor` `@Table` `@Entity` / `@Slf4j` `@RequiredArgsConstructor` `@Service` `@Transactional`

### 4.4 Lombok 사용 규칙

| 어노테이션 | 사용 여부 | 비고 |
|---|---|---|
| `@Getter` | 허용 | 엔티티 기본 |
| `@RequiredArgsConstructor` | 허용 | 생성자 주입용 |
| `@NoArgsConstructor(access = PROTECTED)` | **엔티티 필수** | Checkstyle 검사 |
| `@NoArgsConstructor(access = PRIVATE)` | 허용 | 유틸 클래스 (`DateConverter`) |
| `@Slf4j` | 허용 | 로거는 이것만 사용 (`LoggerFactory` import 금지) |
| `@Builder` | 지양 | 생성은 정적 팩토리 `create(...)` 로 통일 |
| `@Setter` | **엔티티 금지** | DTO 에서도 지양 |
| `@Data` | **금지** | import 자체가 Checkstyle 오류 |
| `@AllArgsConstructor` | 지양 | Checkstyle warning |
| `@ToString` | 엔티티 금지 | 연관관계 순환 참조 위험 |
| `@EqualsAndHashCode` | 엔티티 금지 | `BaseEntity` 가 `id` 기준으로 이미 선언 |

### 4.5 기타

- 의존성 주입은 **생성자 주입**만 사용한다. (`@Autowired` 필드/세터 주입 금지, 테스트 코드는 허용)
- `null` 반환 대신 `Optional` 또는 빈 컬렉션을 반환한다. `Optional` 을 필드나 파라미터로 사용하지 않는다.
- 매직 넘버·매직 스트링은 상수 또는 Enum 으로 추출한다.
- Request / Response / Command / Result / Event / Message 는 **`record`** 로 작성한다.
- 주석은 "무엇"이 아니라 "왜"를 설명한다. 주석 처리된 코드는 커밋하지 않는다.
- `TODO` 에는 담당자와 이슈 번호를 기재한다: `// TODO(할로우, #123): 재시도 정책 적용`

---

## 5. 계층별 작성 규칙

### 5.1 Controller (interfaces)

- 요청 형식 검증, DTO 변환, Service 호출, 응답 반환만 담당한다. **비즈니스 로직·비즈니스 검증 금지.**
- 요청 형식 검증은 Request DTO 의 Bean Validation + `@Valid` 로 하고, 불가피할 때만 DTO 로직으로 한다. ([8](#8-검증validation))
- `{Domain}ApiSpec` 인터페이스를 구현한다. springdoc 어노테이션(`@Tag`, `@Operation`, `@Schema`)은 ApiSpec 에만 쓰고, Controller 에는 Spring MVC 어노테이션만 둔다.
- 반환 타입은 항상 **`ApiResponse<T>`** 다. 엔티티나 application 의 `Result` 를 그대로 응답하지 않는다.
- 요청자는 `@RequestHeader("X-USER-ID") Long userId` 로 받는다.

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController implements OrderApiSpec {

    private final OrderService orderService;

    @Override
    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailResponse> getOrder(
            @RequestHeader("X-USER-ID") Long userId, @PathVariable(value = "orderId") Long orderId) {

        OrderDetailResult order = orderService.getOrder(userId, orderId);

        return ApiResponse.success(OrderDetailResponse.from(order));
    }
}
```

```java
@Tag(name = "Order API", description = "Order API 입니다.")
public interface OrderApiSpec {
    @Operation(summary = "단일 주문 상세 조회", description = "요청자 소유의 주문 상세 정보를 조회합니다.")
    ApiResponse<OrderDetailResponse> getOrder(
            @Schema(name = "X-USER-ID", description = "요청자의 유저 ID (X-USER-ID 헤더)") Long userId,
            @Schema(name = "주문 ID", description = "조회할 주문 ID") Long orderId);
}
```

### 5.2 Application Service (application)

- 도메인당 `{Domain}Service` 하나를 두고, 유스케이스 단위로 메서드를 작성한다. 트랜잭션 경계를 여기서 정의한다.
- 클래스에 `@Service` + `@Transactional(readOnly = true)` 를 선언하고, 변경 메서드에만 `@Transactional` 을 붙인다.
- 흐름은 **조회 → 도메인 메서드 호출 → 이벤트 발행** 으로 단순하게 유지한다. 규칙 판단은 엔티티와 Domain Service 에 위임한다. (규모 작음의 기능은 서비스 메서드에 흐름대로 둔다. [2.2](#22-설계-규모와-ddd))
- domain 의 Repository **인터페이스**와 Domain Service 에만 의존한다.
- 반환 타입은 `Result`(application DTO)다. interfaces 의 `Response` 를 반환하지 않는다.
- 영속 상태 엔티티의 변경은 변경 감지(dirty checking)에 맡긴다. 신규 엔티티만 `save` 한다.

```java
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    public OrderDetailResult getOrder(Long userId, Long orderId) {
        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("[orderId = " + orderId + "] 주문을 찾을 수 없습니다."));

        // 타인의 주문은 존재 여부를 노출하지 않도록 404 로 응답한다.
        if (!order.getUserId().equals(userId)) {
            throw new EntityNotFoundException("[orderId = " + orderId + "] 주문을 찾을 수 없습니다.");
        }

        return OrderDetailResult.from(order);
    }

    @Transactional
    public void completeOrder(Long orderId) {
        Order order =
                orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));

        order.complete();

        applicationEventPublisher.publishEvent(new OrderCompletedEvent(orderId, order.getOrderItems()));
    }
}
```

### 5.3 이벤트 리스너 (application)

```java
@Slf4j
@RequiredArgsConstructor
@Component
public class OrderEventListener {

    private final OrderService orderService;

    private final OrderMessagePublisher orderMessagePublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentResult(PaymentResultEvent event) {
        if (PaymentResultStatus.SUCCESS.equals(event.status())) {
            orderService.completeOrder(event.orderId());
        } else if (PaymentResultStatus.FAILED.equals(event.status())) {
            orderService.cancelOrderWithRestoration(event.orderId());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCompleted(OrderCompletedEvent event) {
        orderMessagePublisher.publishOrderCompleted(event.toCompletedMessage());
    }
}
```

- 리스너는 자기 도메인의 Service 와 MessagePublisher 만 호출한다. 리스너에 비즈니스 로직을 쓰지 않는다.
- 응답 시간과 무관한 후속 처리(좋아요 수 집계, 데이터 플랫폼 전송)는 `@Async` 를 함께 붙인다.
- `@Async` 스레드에서도 요청자 정보가 유지되도록 `AsyncContextPropagationConfig` 가 `UserContextHolder` 를 전파한다.

### 5.4 Entity (domain)

```java
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "point")
@Entity
public class Point extends BaseEntity {

    private static final long MIN_CHARGE_AMOUNT = 100L;
    private static final long MIN_USE_AMOUNT = 100L;

    private Long userId;

    private Long balance;

    private Point(Long userId) {
        this.userId = userId;
        this.balance = 0L;
    }

    public static Point create(Long userId) {
        return new Point(userId);
    }

    public void charge(Long amount) {
        validateChargeAmount(amount);
        this.balance += amount;
    }

    public void use(Long amount) {
        validateUseAmount(amount);
        this.balance -= amount;
    }

    private void validateChargeAmount(Long amount) {
        if (amount < MIN_CHARGE_AMOUNT) {
            throw new IllegalArgumentException("100 이상의 포인트를 충전할 수 있습니다.");
        }
    }

    private void validateUseAmount(Long useAmount) {
        if (useAmount < MIN_USE_AMOUNT) {
            throw new IllegalArgumentException("포인트는 100원 이상 사용할 수 있습니다.");
        }

        if (this.balance < useAmount) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }
    }
}
```

- `@Table(name = ...)` 을 항상 명시한다. (`DatabaseCleanUp` 이 `@Table.name` 으로 테이블을 찾는다.)
- 상태 전이 조건은 엔티티가 판단한다. (`Order.pending()` 은 `CREATED` 에서만 가능)
- 필수 텍스트 검증은 `TextValidator.requireText(value, message)` 를 쓴다.
- 다른 도메인의 엔티티는 **ID(`Long userId`, `Long orderId`)로 참조**하는 것을 기본으로 한다. 조회 시 조인이 꼭 필요한 관계만 `@ManyToOne(fetch = LAZY)` 로 매핑한다.

### 5.5 DTO

변환 흐름과 책임:

```
Request ──toCommand()──▶ Command ──toEntity() / CreateInfo──▶ Entity
                                                               │
Response ◀──from(result)── Result ◀──────from(entity)──────────┘
```

| DTO | 위치 | 변환 메서드 |
|---|---|---|
| `*Request` | `interfaces/api/{domain}/dto` | `toCommand()`, `toCondition()` |
| `*Response` | `interfaces/api/{domain}/dto` | `static from(Result)` |
| `*Command`, `*Condition` | `application/{domain}/dto` | `toEntity(...)` |
| `*Result` | `application/{domain}/dto` | `static from(Entity)` |
| `*CreateInfo` | `domain/{domain}/dto` | - |

- Request/Response 는 interfaces 밖으로, Entity 는 application 밖으로 나가지 않는다.
- 쿼리 파라미터는 `record` + `@ModelAttribute` 로 받는다. 기본값은 record 생성자에서 채운다. (`ProductsCondition`)
- `@Cacheable` 로 캐시되는 Result 는 `Serializable` 을 구현한다. (`ProductResult`, `ProductsResult`)

```java
public record OrderDetailResponse(
        Long orderId,
        OrderStatus status,
        Long totalAmount,
        List<OrderItemResponse> items,
        ZonedDateTime orderedAt) {
    public static OrderDetailResponse from(OrderDetailResult result) {
        List<OrderItemResponse> items =
                result.items().stream().map(OrderItemResponse::from).toList();

        return new OrderDetailResponse(result.id(), result.status(), result.totalAmount(), items, result.orderedAt());
    }
}
```

---

## 6. API 설계 규칙

### 6.1 URL

- 형식: **`/api/{resource}`** — 버전 프리픽스(`/v1`)를 쓰지 않는다.
- 리소스는 **복수형 명사, 소문자, kebab-case**: `/api/orders`, `/api/products`
- 동사 사용 금지. 행위가 필요하면 하위 리소스로 표현한다: `POST /api/points/charge`, `POST /api/payments/order-payment`
- 요청자 본인의 리소스는 `/me` 로 표현한다: `GET /api/users/me`
- 쿼리 파라미터는 camelCase: `?searchKeyword=나이키&sortBy=PRICE_ASC`

현재 엔드포인트:

| Method | URL | 설명 |
|---|---|---|
| POST | `/api/users` | 회원 가입 (인증 제외) |
| GET | `/api/users/me` | 내 정보 조회 |
| GET | `/api/brands`, `/api/brands/{brandId}` | 브랜드 목록 / 상세 |
| GET | `/api/products`, `/api/products/{productId}` | 상품 목록 / 상세 |
| POST / DELETE | `/api/like/products/{productId}` | 좋아요 등록 / 취소 |
| GET | `/api/like/products` | 내가 좋아요한 상품 목록 |
| GET / POST | `/api/points`, `/api/points/charge` | 포인트 조회 / 충전 |
| POST / GET | `/api/orders` | 주문 생성 / 내 주문 목록 |
| GET | `/api/orders/{orderId}`, `/api/orders/{orderId}/status` | 주문 상세 / 상태 |
| POST | `/api/payments/order-payment` | 결제 요청 |
| POST | `/api/payments/callback` | PG 결제 결과 콜백 (인증 제외) |

### 6.2 HTTP Method & Status

| Method | 용도 |
|---|---|
| GET | 조회 |
| POST | 생성 / 액션 |
| DELETE | 삭제 |

- 성공 응답은 모두 **200** 이다. Controller 가 `ApiResponse` 를 직접 반환하며 `ResponseEntity` 로 201/204 를 구분하지 않는다.
- 실패 응답의 상태 코드는 `ErrorType` 이 결정한다.

| 코드 | ErrorType | 의미 |
|---|---|---|
| 400 | `BAD_REQUEST` | 요청 형식/검증 오류, 도메인 규칙 위반 |
| 401 | - | `X-USER-ID` 가 존재하지 않는 사용자 (인터셉터) |
| 404 | `NOT_FOUND` | 리소스 없음 |
| 409 | `CONFLICT` | 이미 존재하는 리소스 |
| 500 | `INTERNAL_ERROR` | 서버 오류 |

### 6.3 응답 포맷

성공과 실패 모두 **`ApiResponse<T>`** 로 감싼다. `null` 필드는 직렬화하지 않는다. (`supports/jackson` 의 `NON_NULL`)

**성공**

```json
{
  "meta": { "result": "SUCCESS" },
  "data": {
    "orderId": 1,
    "status": "PENDING",
    "totalAmount": 15000,
    "orderedAt": "2026-10-05T17:00:00+09:00"
  }
}
```

**실패**

```json
{
  "meta": {
    "result": "FAIL",
    "errorCode": "Not Found",
    "message": "[orderId = 1] 주문을 찾을 수 없습니다."
  }
}
```

- 응답 본문이 없는 성공은 `ApiResponse.success()` 를 반환한다.
- `errorCode` 는 `ErrorType.code` (HTTP reason phrase) 다. `message` 는 예외의 메시지가 있으면 그것을, 없으면 `ErrorType.message` 를 쓴다.

### 6.4 페이징

- **offset / size** 방식을 쓴다: `?offset=0&size=20`
- 기본값은 `offset=0`, `size=20` 이며 Condition record 가 채운다.
- 응답은 페이지 메타 없이 **목록(`List`)** 을 `data` 에 그대로 담는다.

### 6.5 날짜/시간

- 시각은 `ZonedDateTime` 으로 다루고 ISO-8601 오프셋 형식으로 직렬화한다: `2026-10-05T17:00:00+09:00`
- 날짜만 받는 입력은 `yyyy-MM-dd` 문자열로 받아 `DateConverter.convertToLocalDate()` 로 변환한다.
- 애플리케이션 기본 타임존은 **`Asia/Seoul`** (`CommerceApiApplication`), DB 저장은 **UTC** 로 정규화한다. (`hibernate.timezone.default_storage: NORMALIZE_UTC`, `jdbc.time_zone: UTC`)

### 6.6 사용자 식별

- 별도의 인증 체계 없이 요청 헤더 **`X-USER-ID`** (= `member.id`, 숫자 PK)로 요청자를 식별한다.
- `UserAuthenticationInterceptor` 가 모든 경로에서 헤더를 검사한다.
  - 헤더 누락 / 숫자가 아님 → 400, 존재하지 않는 사용자 → 401
  - 제외 경로: `POST /api/users`, `/api/payments/callback`, Swagger 관련 경로 (`WebConfig`)
- `UserContextFilter` 가 헤더 값을 `UserContextHolder`(ThreadLocal)에 넣고 요청 종료 시 비운다.
- 인증이 필요 없는 경로를 추가하면 `WebConfig.excludePathPatterns` 에 등록한다.

---

## 7. 예외 처리

### 7.1 구조

```
RuntimeException
├── CoreException (ErrorType 보유)          → ErrorType 의 status
├── IllegalArgumentException                → 400
├── IllegalStateException                   → 400
└── jakarta.persistence.EntityNotFoundException → 404
```

### 7.2 ErrorType

```java
@Getter
@RequiredArgsConstructor
public enum ErrorType {
    /** 범용 에러 */
    INTERNAL_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "일시적인 오류가 발생했습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "잘못된 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "존재하지 않는 요청입니다."),
    CONFLICT(HttpStatus.CONFLICT, HttpStatus.CONFLICT.getReasonPhrase(), "이미 존재하는 리소스입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
```

- `ErrorType` 은 **범용 4종**만 둔다. 도메인별 에러 코드를 만들지 않고, 상황 설명은 예외 메시지로 전달한다.
- 새 `ErrorType` 이 필요하면 HTTP 상태와 1:1 로 대응되는지 먼저 확인한다.

### 7.3 어떤 예외를 던지는가

| 상황 | 예외 | 응답 |
|---|---|---|
| 입력값이 도메인 규칙에 맞지 않음 (금액 부족, 형식 오류) | `IllegalArgumentException` | 400 |
| 현재 상태에서 할 수 없는 동작 (이미 사용된 쿠폰, 만료) | `IllegalStateException` | 400 |
| 조회 대상이 없음 | `EntityNotFoundException` | 404 |
| 위에 해당하지 않고 상태 코드를 직접 지정해야 함 (중복 → 409) | `new CoreException(ErrorType.CONFLICT, "메시지")` | ErrorType |

- 조회 실패 메시지는 식별자를 포함한다: `"[orderId = " + orderId + "] 주문을 찾을 수 없습니다."`
- 메시지는 그대로 클라이언트에 노출되므로 **사용자가 읽을 수 있는 한국어 문장**으로 쓴다.

### 7.4 규칙

- 예외 → 응답 변환은 `interfaces/api/ApiControllerAdvice` 한 곳에서만 한다. Controller 에서 `try-catch` 로 응답을 만들지 않는다.
- 요청 바인딩 오류(`HttpMessageNotReadableException`, `BindException`, `MethodArgumentTypeMismatchException`, `MissingServletRequestParameterException`)는 Advice 가 400 과 원인 필드를 담은 메시지로 변환한다.
- 예외를 잡아서 아무 처리 없이 삼키지 않는다. 주석만 있는 `catch` 블록도 Checkstyle 오류다.
- 예외를 변환할 때는 **원인(cause)을 포함**한다.
- `CoreException`·바인딩 오류 → `WARN`, 예상치 못한 예외(`Throwable`) → `ERROR` + 스택트레이스
- 흐름 제어 목적으로 예외를 사용하지 않는다.
- 내부 구현 정보(SQL, 스택트레이스, 클래스명)를 응답에 노출하지 않는다. 500 응답은 항상 `ErrorType.INTERNAL_ERROR` 의 기본 메시지를 쓴다.
- 외부 호출 실패는 예외를 전파하지 않고 **fallback 결과**(`PaymentResultStatus.FAILED`)로 변환한다.

---

## 8. 검증(Validation)

검증은 **요청 형식 검증**과 **비즈니스 검증**으로 나누고, 계층마다 하나만 맡는다.

- **interfaces(Controller · Request DTO)**: 요청 값이 올바른 형식인지만 본다. 필수값, 타입, 길이, 범위, 패턴, 필드 간 형식 관계(시작일 ≤ 종료일).
- **application · domain(Application Service · Entity · VO · Domain Service)**: 비즈니스 정책과 요구사항을 본다. 존재 여부, 소유자, 상태 전이, 잔액·재고, 중복, 정책상 최소/최대값.

| 위치 | 검증 대상 | 방법 |
|---|---|---|
| 요청 바인딩 | 타입, enum 값, JSON 형식 | Spring/Jackson → `ApiControllerAdvice` 가 400 변환 |
| Request DTO (1순위) | 필수값, 길이, 범위, 패턴 | Bean Validation (`@NotNull`, `@Size`, `@Positive`, `@Pattern` ...) + Controller 의 `@Valid` |
| Request DTO (2순위) | 어노테이션으로 표현할 수 없는 형식 규칙 | `toCommand()` / `toCondition()` 에서 직접 검증 → `CoreException(ErrorType.BAD_REQUEST, ...)` |
| Application Service | 존재 여부, 소유자 확인, 여러 애그리거트에 걸친 정책 | 조회 후 `EntityNotFoundException`, 정책 위반 시 [7.3](#73-어떤-예외를-던지는가) 의 예외 |
| Entity / VO / Domain Service | 도메인 불변식, 상태 전이, 비즈니스 규칙 | 팩토리·도메인 메서드에서 `IllegalArgumentException` / `IllegalStateException` |

- **요청 형식 검증은 Bean Validation 을 가장 먼저 쓴다.** 어노테이션으로 표현할 수 없을 때만 DTO 안에 로직으로 쓴다. Controller 메서드 본문에 `if` 검증을 쓰지 않는다.
- DTO 의 로직 검증도 **형식 검증에 한정**한다. Repository 조회나 비즈니스 정책 판단이 필요하면 application/domain 으로 보낸다.
- **비즈니스 정책과 요구사항은 Controller · DTO 에서 검증하지 않는다.** application 또는 domain 이 판단한다.
- **도메인 불변식은 반드시 엔티티가 검증한다.** 같은 값을 DTO 에서 형식으로 이미 막았더라도(예: `@Positive`) 엔티티의 불변식 검증은 생략하지 않는다. 엔티티는 HTTP 외의 경로(이벤트, 배치)로도 생성·변경된다.
- 구분 기준: "API 를 거치지 않고 호출해도 지켜져야 하는가?" → 그렇다면 비즈니스 검증이다.

```java
public record CouponIssueRequest(
        @NotNull(message = "쿠폰 ID를 입력해주세요.") Long couponId,
        @NotNull(message = "시작일을 입력해주세요.") LocalDate startDate,
        @NotNull(message = "종료일을 입력해주세요.") LocalDate endDate) {

    // 어노테이션으로 표현할 수 없는 필드 간 형식 규칙만 DTO 로직으로 검증한다.
    public CouponIssueCommand toCommand(Long userId) {
        if (startDate.isAfter(endDate)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "시작일은 종료일보다 늦을 수 없습니다.");
        }
        return new CouponIssueCommand(userId, couponId, startDate, endDate);
    }
}
```

- record compact 생성자에서 예외를 던지지 않는다. Jackson 역직렬화 중에 던진 예외는 `HttpMessageNotReadableException` 으로 감싸져 메시지가 바뀐다.
- Bean Validation 어노테이션에는 `message` 를 한국어로 지정한다: `@NotNull(message = "충전할 포인트 금액을 입력해주세요.")`
- 공통 검증 유틸은 `support/validation` 에 둔다. (`TextValidator`)
- 저장 직전에 한 번 더 확인해야 하는 불변식은 `BaseEntity.guard()` 를 오버라이드한다. (`@PrePersist`, `@PreUpdate` 시점에 호출)

---

## 9. 트랜잭션

- Application Service 클래스에 `@Transactional(readOnly = true)`, 변경 메서드에만 `@Transactional` 을 선언한다.
- `@Transactional` 은 **application 계층**(Service, EventListener)에서 선언한다.
- **트랜잭션 안에서 외부 API 호출, 메시지 발행을 하지 않는다.** `@TransactionalEventListener(phase = AFTER_COMMIT)` 리스너에서 처리한다.
- 같은 클래스 내부 호출(self-invocation)은 프록시가 적용되지 않음에 유의한다.
- 체크 예외는 기본 롤백 대상이 아니므로, 필요 시 `rollbackFor` 를 명시한다.
- 스케줄러는 `@ConditionalOnProperty` 로 기본 비활성화하고, 준비된 환경에서만 켠다. (`scheduler.payment-recovery.enabled=true`)

### 트랜잭션 경계 나누기

- 트랜잭션은 **선언적 `@Transactional` 로만** 정한다. `TransactionTemplate` 은 쓰지 않는다.
- 경계를 나눠야 하면 **별도 빈으로 분리**하고 그 빈의 `@Transactional` 메서드를 호출한다. 같은 클래스 안에서 부르면 프록시가 적용되지 않는다. 이 분리는 [2.2](#22-설계-규모와-ddd) 컴포넌트 분리 조건 1(트랜잭션 경계가 다르다)에 해당한다.
- 전파 속성은 **기본 `REQUIRED`** 를 쓴다.
- 외부 호출을 트랜잭션 밖에 두려면 `REQUIRES_NEW` 가 아니라 **호출하는 메서드에 `@Transactional` 을 달지 않고**, DB 변경만 짧은 `@Transactional` 메서드로 나눠 부른다.

```
requestPayment()                ← @Transactional 없음
 ├─ 결제 생성                   ← 별도 빈 @Transactional (REQUIRED), 커밋
 ├─ PG 승인 요청                 ← 트랜잭션 밖
 └─ 결과 반영                   ← 별도 빈 @Transactional (REQUIRED), 커밋
```

- **`REQUIRES_NEW` 는 바깥 트랜잭션이 있고, 그 결과와 상관없이 따로 커밋해야 할 때만** 쓴다.
  - `AFTER_COMMIT` 리스너에서 DB 를 변경할 때 (커밋이 끝난 트랜잭션에는 참여할 수 없다.)
  - 바깥이 롤백돼도 남아야 하는 기록 (예: 실패 이력)
- `REQUIRES_NEW` 를 피하는 이유:
  - 바깥 트랜잭션이 DB 연결을 쥔 채 멈추고 새 연결을 하나 더 쓴다. 요청이 몰리면 커넥션 풀이 바닥나 서로를 기다리며 멈출 수 있다.
  - 안쪽이 먼저 커밋되므로 바깥이 실패해도 안쪽 변경은 남는다. 한 번에 롤백되지 않는다.

### 동시성 제어

| 상황 | 방식 | 적용 예 |
|---|---|---|
| 충돌이 잦은 재고 차감 | 비관적 락 (`@Lock(PESSIMISTIC_WRITE)`) | `ProductStockJpaRepository.findByProductIdsWithStockLock` |
| 중복 생성 방지 | 유니크 제약 | `uk_product_like_user_product (user_id, product_id)` |
| 중복 요청에 안전한 동작 | 멱등하게 구현 (이미 처리됐으면 조용히 반환) | `likeProduct`, `unlikeProduct`, `BaseEntity.delete()` |
| 메시지 중복 소비 | 처리 이력 테이블 | commerce-streamer `event_handled (event_id, event_type)` |

- 여러 행에 락을 걸 때는 **ID 목록을 한 번의 쿼리로** 잠근다. 행마다 따로 잠그면 데드락 위험이 있다.

---

## 10. 영속성

- 엔티티는 `BaseEntity` 를 상속한다. 공통 필드와 동작을 엔티티에 다시 선언하지 않는다.

```java
@MappedSuperclass
@EqualsAndHashCode(of = "id")
@Getter
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Column(name = "deleted_at")
    private ZonedDateTime deletedAt;

    protected void guard() {}       // @PrePersist / @PreUpdate 시점 검증 훅

    public void delete() { ... }    // 멱등한 소프트 삭제
    public void restore() { ... }
}
```

- `BaseEntity` 에는 생성/수정/삭제 정보 외의 컬럼이나 동작을 추가하지 않는다.
- 모든 연관관계는 **`FetchType.LAZY`** 를 명시한다. (`@ManyToOne`, `@OneToOne` 은 Checkstyle 검사)
- `@Enumerated(EnumType.STRING)` 필수 (`ORDINAL` 금지, Checkstyle 검사)
- N+1 은 QueryDSL `fetchJoin()` 과 `default_batch_fetch_size: 100` 으로 해결한다.
- 컬렉션 fetch join 과 페이징을 함께 사용하지 않는다.
- 양방향 연관관계는 꼭 필요한 경우에만 사용한다.
- `open-in-view: false` 다. 지연 로딩은 트랜잭션 안(Service 의 `Result.from(entity)`)에서 끝낸다.
- 대량 변경은 벌크 쿼리를 사용하고 `@Modifying(clearAutomatically = true)` 로 영속성 컨텍스트를 정리한다. (Checkstyle 검사)
- 네이티브 쿼리 사용 시 사유를 주석으로 남긴다.
- QueryDSL Q 클래스는 annotation processor 가 생성한다. 생성 코드(`src/*/generated/**`)는 포맷 대상에서 제외된다.
- 집계 조회는 엔티티가 아닌 `domain/{domain}/dto/result` 의 record 로 `Projections.constructor(...)` 프로젝션한다.

---

## 11. 데이터베이스

### 11.1 네이밍

| 대상 | 규칙 | 예시 |
|---|---|---|
| 테이블 | snake_case, **단수형** | `product`, `order_item`, `product_like_count` |
| 컬럼 | snake_case (필드명에서 자동 변환) | `user_id`, `delivery_address` |
| PK | `id` (BIGINT, AUTO_INCREMENT) | |
| FK 컬럼 | `{참조 대상}_id` | `order_id`, `brand_id` |
| 유니크 | `uk_{테이블}_{컬럼}` | `uk_product_like_user_product` |
| 인덱스 | `idx_{테이블}_{컬럼}` | `idx_orders_user_id` |
| Boolean | `is_` 접두사 | `is_used` |
| 일시 | `_at` 접미사 / 일자 `_date` | `created_at`, `birth_date` |

- 예약어와 겹치는 이름은 피한다. 주문은 `orders`, 회원은 `member` 테이블을 쓴다. (`order`, `user` 는 예약어)
- 컬럼명은 Spring Boot 기본 네이밍 전략(camelCase → snake_case)을 따른다. 연관관계 컬럼만 `@JoinColumn(name = ...)` 으로 명시한다.

### 11.2 규칙

- 모든 테이블에 `created_at`, `updated_at`, `deleted_at` 을 둔다. (`BaseEntity`)
- 문자셋: `utf8mb4`, 콜레이션: `utf8mb4_general_ci`
- 금액·수량·포인트는 **`Long`(원 단위)** 을 쓴다. 비율(쿠폰 할인율)은 `BigDecimal`. `double/float` 금지.
- 삭제 정책은 도메인별로 정하고 엔티티 Javadoc 에 남긴다.
  - 기본: 소프트 삭제 (`BaseEntity.delete()` → `deleted_at`)
  - `product_like`: 하드 삭제 (행 존재 = 현재 좋아요 유지)
- 소프트 삭제를 쓰는 테이블의 조회 쿼리에는 `deleted_at is null` 조건을 직접 넣는다. (자동 필터 없음)
- 조회 조건/정렬/조인 컬럼에는 인덱스를 검토한다.

### 11.3 스키마 관리

마이그레이션 도구를 쓰지 않고 Hibernate `ddl-auto` 로 관리한다. (`modules/jpa/src/main/resources/jpa.yml`)

| 프로파일 | `ddl-auto` | 비고 |
|---|---|---|
| (기본) dev / qa / prd | `none` | 스키마는 수동 반영 |
| local | `update` | 엔티티 변경 시 자동 반영 |
| test | `create` | 테스트 시작 시 재생성 |

- 엔티티 매핑을 바꾸면 PR 본문에 **dev 이상 환경에 적용할 DDL** 을 적는다.
- 컬럼 삭제·타입 변경처럼 `update` 로 반영되지 않는 변경은 로컬에서도 수동 DDL 이 필요하다.

---

## 12. 캐시 / 메시징

### 12.1 Redis

- 구성: master 1 + read replica 1 (`modules/redis`)
  - 기본 `RedisTemplate<String, String>` : `REPLICA_PREFERRED` (조회용)
  - `@Qualifier(RedisConfig.REDIS_TEMPLATE_MASTER)` : master 전용 (쓰기, 쓴 직후 읽기)
- `RedisTemplate` 의 key/value 직렬화는 `StringRedisSerializer` 다. 객체는 JSON 문자열로 변환해서 넣는다.
- 조회 캐시는 Spring Cache (`@Cacheable`) 를 Application Service 에 적용한다.

```java
@Cacheable(value = "products", key = "#id")
public ProductResult getProduct(Long id) { ... }
```

- 키 형식: `{도메인}:{용도}:{식별자}` 예) `ranking:order:2026-10-05`
- **모든 키에 TTL 을 설정**한다. (영구 키는 사유 명시)
- 목록 캐시는 조회 빈도가 높은 구간만 캐시한다. (`ProductService.getAll` 은 앞쪽 3페이지만)
- 변경 시에는 갱신보다 삭제(evict)를 우선한다.
- `KEYS *` 금지 → `SCAN` 사용
- 캐시 장애 시 DB 로 폴백 가능하도록 작성한다.

### 12.2 Kafka

- 토픽명: **`{도메인}.v{버전}`** — `order.v1`, `product.v1`, `audit.v1`
- 토픽명은 코드에 쓰지 않고 `application.yml` 의 `kafka.topics.*` 에서 주입한다.
- 메시지는 **Envelope** 로 감싼다.

```java
public record OrderMessageEnvelope<T>(
        OrderMessageType eventType,   // ORDER_COMPLETED
        String eventId,               // UUID, 멱등 처리 키
        String aggregateId,           // 주문 ID
        String aggregateType,         // "ORDER"
        ZonedDateTime occurredAt,
        T payload) {}
```

- **메시지 키는 `aggregateId`** 로 지정해 같은 애그리거트의 순서를 보장한다.
- 도메인 토픽에 발행한 메시지는 **`audit.v1` 에도 함께 발행**한다. (commerce-streamer 가 `event_log` 로 적재)
- Producer: `acks=all`, `enable.idempotence=true`, `retries=3` (`modules/kafka/kafka.yml`)
- Consumer: 수동 커밋(`ack-mode: manual`), 배치 리스너(`KafkaConfig.BATCH_LISTENER`)
- 발행 포트는 `domain/{domain}/message/{Domain}MessagePublisher`, 구현은 `infrastructure/{domain}/kafka` 에 둔다.
- 메시지 타입을 추가하면 `{Domain}MessageType` enum 과 commerce-streamer 의 컨슈머 분기를 함께 수정한다.
- 토픽 자동 생성은 꺼져 있다(`auto.create.topics.enable: false`). 새 토픽은 미리 만든다.

**소비 측 (commerce-streamer)**

- 컨슈머는 `interfaces/consumer` 에 둔다.
- 메시지는 **멱등하게 처리**한다. 처리 전 `event_handled` 에서 `(eventId, eventType)` 을 확인하고, 처리 후 기록한 다음 `acknowledge()` 한다.
- 집계 결과는 `product_metrics`(좋아요 수, 조회 수, 판매량)에 저장한다.

---

## 13. 로깅

- `@Slf4j` 사용. `LoggerFactory` 직접 사용, `System.out` / `System.err`, `e.printStackTrace()` 금지 (Checkstyle 검사)
- 문자열 연결 대신 플레이스홀더 사용: `log.info("Payment requested for orderId: {}", event.orderId())`
- 예외를 로그에 남길 때는 마지막 인자로 예외 객체를 넘겨 스택트레이스를 남긴다: `log.error("... {}", e.getMessage(), e)`
- 요청 추적용 `traceId` / `spanId` 는 Micrometer Tracing(Brave)이 MDC 에 넣는다.

| 레벨 | 용도 |
|---|---|
| ERROR | 예상치 못한 예외, 외부 호출 실패(fallback 진입), 배치 건별 실패 |
| WARN | `CoreException`, 요청 바인딩 오류 |
| INFO | 주요 비즈니스 이벤트 (이벤트 리스너 진입, 스케줄러 시작/종료) |
| DEBUG | 개발/디버깅용 상세 정보 |

- **개인정보·민감정보 로그 금지**: 카드번호, 연락처, 배송지 등. 필요 시 마스킹.
- 반복문 내부에서 INFO 이상 로그를 남기지 않는다. (Checkstyle 검사, 예외는 사유와 함께 억제)

프로파일별 출력 (`supports/logging/src/main/resources/logback/logback.xml`):

| 프로파일 | 포맷 | `com.loopers` 레벨 |
|---|---|---|
| local, test | 일반 콘솔 | DEBUG |
| dev | JSON (logstash 구조화) | DEBUG |
| qa, prd | JSON (logstash 구조화) | INFO |

- Slack appender 는 구성되어 있으나 비활성 상태다. 켜려면 `properties/slack-log-{profile}.xml` 을 설정하고 `ASYNC-SLACK` 참조의 주석을 푼다.

---

## 14. 설정 / 프로파일

### 14.1 구조

```
apps/commerce-api/src/main/resources/application.yml   # 앱 설정 + 프로파일별 문서(---)
modules/jpa/src/main/resources/jpa.yml                 # DataSource, Hibernate
modules/redis/src/main/resources/redis.yml             # master / replica
modules/kafka/src/main/resources/kafka.yml             # producer / consumer
supports/logging/src/main/resources/logging.yml
supports/monitoring/src/main/resources/monitoring.yml  # actuator(8081), prometheus
```

- 프로파일: **`local`(기본), `test`, `dev`, `qa`, `prd`**
- 프로파일별 파일을 나누지 않고, 한 yml 안에서 `---` + `spring.config.activate.on-profile` 로 구분한다.
- 모듈 설정은 모듈의 yml 에 두고 앱에서 `spring.config.import` 로 가져온다. 앱의 `application.yml` 에 인프라 설정을 복사하지 않는다.

### 14.2 규칙

- 비밀 정보는 **레포에 커밋하지 않는다.** 환경 변수로 주입한다: `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_USER`, `MYSQL_PWD`, `REDIS_MASTER_HOST`, `BOOTSTRAP_SERVERS` ...
- 커스텀 설정은 `@ConfigurationProperties` + **`record`** 로 바인딩한다. (`class` 로 작성하면 Checkstyle 오류, `@Value` 는 warning)

```java
@ConfigurationProperties(value = "datasource.redis")
public record RedisProperties(int database, RedisNodeInfo master, List<RedisNodeInfo> replicas) {}
```

- `prd` 프로파일에서는 API 문서를 끈다. (`springdoc.api-docs.enabled: false`)
- 테스트는 Gradle 이 `spring.profiles.active=test`, `user.timezone=Asia/Seoul` 로 실행한다.

### 14.3 외부 호출 설정

- 외부 호출에는 반드시 **connect/read timeout** 을 설정한다. (PG: connect 1s / read 3s, `FeignClientConfig`)
- CircuitBreaker·Retry 인스턴스는 `application.yml` 의 `resilience4j.*.instances` 에 **용도별 이름**으로 등록한다: `pgRequestPaymentConfig`, `pgRequestPaymentRetryConfig`
- 인스턴스를 추가할 때는 `configs.default` 를 `baseConfig` 로 상속하고 달라지는 값만 덮어쓴다.

### 14.4 로컬 실행

```shell
docker-compose -f ./docker/infra-compose.yml up        # MySQL 3306, Redis 6379/6380, Kafka 19092, Kafka UI 9091
docker-compose -f ./docker/monitoring-compose.yml up   # Prometheus, Grafana 3000 (admin/admin)
./gradlew :apps:pg-simulator:bootRun                   # 8082
./gradlew :apps:commerce-api:bootRun                   # 8080, Swagger: /swagger-ui.html
./gradlew :apps:commerce-streamer:bootRun              # 8085
```

---

## 15. 테스트

기준을 검증하는 테스트는 **BDD 형식의 시나리오**로 쓴다. 테스트가 확인하는 것은 코드의 동작이 아니라 **도메인 정책**이다.

> 이 장의 requirements · design 은 spec-mirror 명세(`docs/spec/{기능}/`)를 말한다.

### 15.1 쓰는 원칙

1. **시나리오에 집중한다.** 테스트 하나는 시나리오 하나다. 상황(given) · 행동(when) · 결과(then)로 쓴다.
2. **개발자가 아니어도 읽을 수 있게 쓴다.** 테스트 이름과 given / when / then 은 requirements 의 도메인 용어로 쓴다. 응답 코드, 에러 코드, 영문 상태 값, API 경로, 클래스 · 메서드 이름을 쓰지 않는다.
3. **현상이 아니라 정책을 쓴다.** 응답 · 메시지 · 상태 값 · 호출 횟수 · 로그처럼 테스트가 관찰하는 것을 쓰지 않고, 그 관찰이 뜻하는 정책을 쓴다.

| 현상 (쓰지 않음) | 정책 (씀) |
|---|---|
| 결제 기한 정각에 승인을 요청하면 `400` `PAYMENT_EXPIRED`를 응답한다 | 결제 기한이 되면 결제할 수 없다 |
| 결제사 승인 요청이 한 번만 호출된다 | 같은 주문을 동시에 여러 번 결제해도 결제는 한 번만 이루어진다 |
| 결제 상태가 `FAILED`, 주문 상태가 `PAYMENT_FAILED`가 된다 | 주문은 결제 실패가 된다 |

### 15.2 테스트 종류와 맡는 기준

| 종류 | 대상 | 클래스명 | 테스트 패키지 | 도구 | 거절을 확인하는 방법 |
|---|---|---|---|---|---|
| 도메인 단위 테스트 | Entity, VO, Domain Service | `{Entity}Test` | `domain/{domain}` | JUnit 5 + AssertJ (Spring 없이) | 예외 타입 + 메시지 |
| 서비스 통합 테스트 | Application Service | `{Domain}ServiceIntegrationTest` | `domain/{domain}` | `@SpringBootTest` | 예외 타입 + 메시지 |
| 리포지토리 통합 테스트 | RepositoryImpl 쿼리 | `{Entity}RepositoryImplIntegrationTest` | `infrastructure/{domain}/jpa` | `@SpringBootTest` | - |
| E2E 테스트 | API 요청 ~ 응답 | `{Domain}ApiE2eTest` | `interfaces/api/{domain}` | `@SpringBootTest(RANDOM_PORT)` + `TestRestTemplate` | HTTP 상태 + `ApiResponse` 의 `errorCode` · `message` |

- 기준은 **그 판단을 하는 코드를 확인하는 테스트**가 맡는다. 판단 위치는 design 02 에 있고, 기준마다 어떤 테스트가 맡는지는 design 05 의 `기준 ↔ 테스트` 표에 쓴다.
  - 요청 형식 검증([8](#8-검증validation))은 Controller · Request DTO 에서 판단하므로 E2E 테스트가 맡는다.
  - 도메인 불변식 · 상태 전이는 도메인 단위 테스트가 맡는다. **가장 두텁게** 쓰고, 도메인 규칙을 확인하려고 Spring 컨텍스트를 띄우지 않는다.
  - 존재 여부 · 소유자 확인 · 여러 애그리거트에 걸친 정책 · 동시성은 서비스 통합 테스트가 맡는다.
- 여러 테스트에서 같은 기준을 함께 확인해도 된다.
- 기존 코드(adopt, 기존 기능 변경)는 지금 코드에서 판단하는 곳을 따른다. 테스트를 위해 코드를 옮기지 않는다.
- 슬라이스 테스트(`@WebMvcTest`, `@DataJpaTest`)는 쓰지 않는다. 컨텍스트를 하나로 유지해 재사용한다.

### 15.3 회원이 받는 응답

- **design 04 의 에러마다 회원이 실제로 받는 응답(응답 코드 · 에러 코드 · 메시지)을 확인하는 시나리오가 하나 이상** 있어야 한다. 기준을 다른 테스트가 맡아도, 회원에게 어떻게 전달되는지는 E2E 대표 시나리오로 확인한다. 어느 시나리오가 확인하는지는 design 05 의 `에러 ↔ 응답 테스트` 표에 쓴다.
- 메시지는 어느 테스트에서 확인하든 requirements 와 글자까지 같아야 한다.

### 15.4 테스트 이름

`<상황>이면 <결과>다`

- 이름은 `@DisplayName` 에 쓴다. 메서드명은 영문 camelCase 를 기본으로 하되 한글 · 언더스코어도 허용한다.
- 테스트 이름만 봐도 상황과 결과를 알 수 있게 쓴다. 기준 문장이 앞 기준에 기대면("대조 결과가 맞으면") 그 상황을 풀어 쓴다.
  - 기준 6.17 "대조 결과가 맞으면 결제를 승인 완료로 바꾼다" → `결제사가 주문과 같은 내용으로 승인하면 결제가 완료된다`
- 문장으로 끝낸다(`~다`). `~불가`, `~성공`처럼 명사로 끝내지 않는다.
- 유스케이스와 항상 지켜야 할 규칙은 `<누가>는 <무엇>을 할 수 있다`, `<상황>이어도 <결과>다`로 쓴다.
  - 예: `같은 주문을 동시에 여러 번 결제해도 결제는 한 번만 이루어진다`
- 다른 테스트가 맡은 기준이 회원에게 전달되는지 확인할 때는 결과를 "안내받는다"로 쓴다.
  - 예: `회원이 주문 금액과 다른 금액으로 결제하면 결제할 수 없다고 안내받는다`

### 15.5 결과 하나에 시나리오 하나

- **결과가 다르면 시나리오를 나눈다.**
- **결과가 같으면 한 시나리오의 예시로 묶는다.** 경계값이나 같은 결과를 내는 여러 조건은 `@ParameterizedTest`(`@ValueSource`, `@MethodSource`, `@NullSource`)로 쓴다.
- requirements `경계:`의 값은 모두 예시에 들어가야 한다. 범위에 양쪽 끝이 있으면 양쪽 모두다.

```
주문 상품이 1개 이상 20개 이하면 주문할 수 있다      예: 1개, 20개
주문 상품이 없거나 20개를 넘으면 주문할 수 없다      예: 0개, 21개
```

### 15.6 본문

- `// given` → `// when` → `// then` 주석으로 나눈다. 이어지는 조건과 결과는 `// and`로 쓴다. 주석도 도메인 용어로 쓴다.
- **when 은 하나**다. 행동이 둘이면 시나리오를 나눈다. 예외를 확인할 때는 when 과 then 이 한 줄로 붙어도 된다(`// when …하면` + `// then …거절된다`).
- **현상 확인은 테스트 지원 코드가 맡는다.** 시나리오 본문에는 `결제가_완료된다()`, `거절된다(결제_거절_사유.결제_기한_만료, ...)`처럼 정책을 부르고, 응답 코드 · 에러 코드 · 메시지 · 상태 값 · 호출 횟수 · 로그 확인은 그 안에서 한다.
- 테스트 지원 코드의 이름도 도메인 용어로 쓴다(한글 메서드명 허용). 회원이 받는 안내 · 거절 사유는 requirements 의 에러 코드 · 메시지와 1:1 인 enum 하나로 둔다.
- 확인하는 것은 줄이지 않는다. 결과로 관찰되는 것(응답 · 에러 코드 · **메시지까지** · 상태 변화 · 만들어지는지 여부 · 외부 시스템 호출 횟수 · 로그)을 그 테스트가 볼 수 있는 만큼 모두 확인한다. 로그는 `support/log/LogCapture` 로 확인한다.

```java
@DisplayName("결제 기한이 되면 결제를 시작할 수 없다")
@ParameterizedTest(name = "기한 {0}")
@ValueSource(strings = {"정각", "1초 후"})
void 결제_기한이_되면_결제를_시작할_수_없다(String 시점) {
    // given 결제 기한이 01:30인 결제가 승인을 기다리고 있다
    Payment 결제 = 승인_대기_결제("01:30");

    // when 결제 기한 <시점>에 결제를 시작하면
    // then 결제 기한이 지났다는 이유로 거절된다
    거절된다(결제_거절_사유.결제_기한_만료, () -> 결제.startApproval(결제_기한_기준(시점)));

    // and 결제는 승인 대기 그대로다
    승인_대기_그대로다(결제);
}
```

### 15.7 테스트 묶음과 환경

- **테스트 묶음:** 테스트 종류마다 Requirement 하나당 클래스 하나를 기본으로 한다. 한 클래스 안에서 유스케이스별로 나눠야 하면 `@Nested` 를 쓴다.
- 외부 시스템(PG, 데이터 플랫폼 등)은 가짜 구현(fake)으로 바꾼다. domain 의 Client · Publisher 인터페이스를 테스트용으로 구현한다.
- 시간은 고정 시계를 쓴다. 현재 시각이 필요한 코드는 `Clock` 을 주입받고, 테스트는 `Clock.fixed(...)` 를 넣는다. `LocalDateTime.now()` 를 직접 부르지 않는다.
- 데이터는 design 05 의 테스트 기준 데이터를 쓴다. 기준 데이터가 아닌 값은 픽스처([15.8](#158-픽스처))가 무작위로 채운다.
- 테스트 간 데이터 의존 금지. 통합 / E2E 테스트는 `@AfterEach` 에서 `databaseCleanUp.truncateAllTables()` 를 호출한다.
- 통합 테스트 DB 는 H2 가 아닌 **Testcontainers MySQL 8.0** 이다. `modules/jpa` testFixtures 의 `MySqlTestContainersConfig` 가 컨테이너를 띄우고 접속 정보를 주입한다.
- 테스트는 직렬로 실행된다. (`maxParallelForks = 1`)
- 테스트 코드에서는 static import, 매직 넘버, `@Autowired` 필드 주입을 허용한다.

### 15.8 픽스처

```
src/test/java/com/loopers/fixture
├── {domain}/{Entity}Fixture.java                     # 인터페이스: create(), create(n), save(), save(n)
├── infra/fixturemonkey/{Entity}FixtureMonkey.java    # Fixture Monkey 기반 구현 (@Component)
├── infra/fixturemonkey/FixtureMonkeyFactory.java
└── support/                                          # CreatedAtBackdater 등 테스트 보조
```

- 테스트는 `{Entity}Fixture` **인터페이스**를 주입받는다. 생성 방식(Fixture Monkey)은 구현 뒤에 숨긴다.
- `create*` 는 메모리 객체만 만들고, `save*` 는 DB 에 저장한다.
- 검증 대상이 되는 값은 무작위로 두지 않고 값을 받는 메서드를 추가한다: `userFixture.save("park")`
- 여러 엔티티가 함께 필요한 상태는 조합 픽스처로 만든다: `ProductWithStockFixture`, `CompletedOrderFixture`

### 15.9 실행

```shell
./gradlew test                               # 전체 (Docker 필요)
./gradlew :apps:commerce-api:test            # 모듈 단위
./gradlew test jacocoTestReport              # 커버리지 XML 리포트
```

- 성능 검증은 `k6/scenarios.js` (유저 워크플로우 가중 시나리오)로 한다. 실행 전 스크립트 상단의 준비 절차를 확인한다.

---

## 16. Git 컨벤션

### 16.1 브랜치 전략 (GitHub Flow)

- `main` 에서 분기하고, PR 로 `main` 에 병합한다.
- 브랜치명: **`{type}/{kebab-case 설명}`**

| 브랜치 | 용도 | 예시 |
|---|---|---|
| `main` | 기준 브랜치 | |
| `feature/{설명}` | 기능 개발 | `feature/infra-toggle` |
| `refactor/{설명}` | 구조 개선 | `refactor/service-layer-consolidation` |
| `fix/{설명}` | 버그 수정 | `fix/order-consumer-topic` |
| `chore/{설명}` | 빌드·설정·도구 | `chore/code-convention-lint` |
| `docs/{설명}` | 문서 | `docs/project-structure-diagram` |

### 16.2 커밋 메시지

```
<type>: <subject>

<body>
```

| type | 의미 |
|---|---|
| `feat` | 기능 추가 |
| `fix` | 버그 수정 |
| `refactor` | 동작 변경 없는 구조 개선 |
| `perf` | 성능 개선 |
| `test` | 테스트 추가/수정 |
| `docs` | 문서 |
| `chore` | 빌드, 설정, 의존성 |
| `style` | 포맷팅 (로직 변경 없음) |

- subject 는 **한국어**, 명사형 종결, 마침표 없음: `feat: 개발용 AWS 인프라 온오프 토글 및 배포 스크립트 추가`
- scope 는 쓰지 않는다.
- 한 커밋에는 한 가지 변경만 담는다. 포맷 변경(`style`)과 로직 변경을 섞지 않는다.
- body 에는 "무엇을, 왜" 변경했는지 적는다. 항목이 여럿이면 `-` 목록으로 쓴다.
- `Co-Authored-By:` 같은 공동 작성자 · 도구 서명 줄을 붙이지 않는다. AI 도구로 커밋할 때도 마찬가지다.

```
refactor: API v1 프리픽스 제거 및 쿼리 파라미터 바인딩 예외 처리 추가

- /api/v1/** 경로를 /api/** 로 통일
- @ModelAttribute 바인딩 실패가 500 으로 응답되던 문제를 400 으로 수정
```

### 16.3 PR 규칙

- 제목: 커밋 메시지 형식과 동일
- PR 을 열면 작성자가 자동으로 assignee 로 지정된다. (`.github/auto_assign.yml`)
- 본문은 `.github/pull_request_template.md` 를 따른다.

| 섹션 | 내용 |
|---|---|
| 📌 Summary | 어떤 기능/이슈를 해결했는지 |
| 💬 Review Points | 중점적으로 봐줬으면 하는 부분, 고민한 설계 포인트, 확인이 필요한 예외 상황 |
| ✅ Checklist | 완료 여부 체크리스트. 미완료 항목은 `TODO -` 로 표기 |
| 📎 References | 참고 문서·링크 (없으면 섹션 삭제) |

- CI 가 구성되어 있지 않으므로 **PR 을 올리기 전 로컬에서 아래를 통과**시킨다.

```shell
./gradlew spotlessApply checkstyleMain checkstyleTest test
```

---

## 17. 코드 리뷰

### 17.1 리뷰 코멘트 prefix

| prefix | 의미 |
|---|---|
| `[필수]` | 반드시 수정 필요 |
| `[제안]` | 개선 제안, 반영 여부는 작성자 판단 |
| `[질문]` | 의도 확인 |
| `[칭찬]` | 좋은 코드 공유 |

### 17.2 체크리스트

- [ ] 요구사항(`docs/design/01-requirements.md`)을 정확히 구현했는가
- [ ] 계층 책임과 의존 방향(`interfaces → application → domain ← infrastructure`)을 지켰는가
- [ ] 클래스가 [2.5 패키지 구조](#25-패키지-구조)와 [3.2 접미사](#32-클래스-접미사) 규칙에 맞는 위치·이름인가
- [ ] 비즈니스 규칙이 Application Service 가 아닌 엔티티·Domain Service 에 있는가
- [ ] 다른 도메인의 상태 변경을 직접 호출이 아닌 이벤트로 처리했는가
- [ ] 트랜잭션 범위가 적절한가 (트랜잭션 내 외부 호출·메시지 발행 없음, `AFTER_COMMIT` + `REQUIRES_NEW`)
- [ ] N+1, 불필요한 쿼리, 인덱스 미사용 쿼리가 없는가
- [ ] 동시성 이슈(재고·포인트 경쟁, 중복 요청) 가능성을 고려했는가
- [ ] 예외 종류와 메시지가 [7.3](#73-어떤-예외를-던지는가) 기준에 맞는가
- [ ] 응답이 `ApiResponse` 로 감싸져 있고 엔티티가 노출되지 않는가
- [ ] 민감정보가 로그/응답에 노출되지 않는가
- [ ] 외부 호출에 timeout / CircuitBreaker / fallback 이 있는가
- [ ] Kafka 메시지 타입을 추가했다면 commerce-streamer 컨슈머와 멱등 처리가 함께 반영되었는가
- [ ] 엔티티 매핑 변경 시 적용할 DDL 이 PR 에 적혀 있는가
- [ ] 테스트가 핵심 시나리오와 실패 케이스를 커버하는가
- [ ] `spotlessCheck`, `checkstyleMain`, `checkstyleTest`, `test` 가 통과하는가

---

## 18. 문서화

- API 문서: springdoc-openapi, 경로 `/swagger-ui.html` (`prd` 프로파일 비활성)
- springdoc 어노테이션은 `{Domain}ApiSpec` 인터페이스와 Request DTO 의 `@Schema` 에 작성한다. ([5.1](#51-controller-interfaces))
- 설계 문서는 구현 전에 `docs/design/` 에 먼저 반영한다.

```
docs/
├── architecture/
│   └── project-structure.excalidraw     # 프로젝트 구조 다이어그램
├── convention/
│   ├── backend-convention.md            # 범용 템플릿
│   └── project-convention.md            # 본 문서
├── design/
│   ├── 01-requirements.md               # 요구사항, 유스케이스
│   ├── 02-sequence-diagrams.md
│   ├── 03-class-diagrams.md
│   └── 04-erd.md
└── superpowers/                         # 작업별 설계(spec) / 실행 계획(plan)
    ├── specs/
    └── plans/
```

- `.gitignore` 가 `docs/*` 를 무시하고 `docs/architecture/` 만 예외로 둔다. 그 밖의 새 문서를 레포에 올리려면 `git add -f` 를 쓰거나 `.gitignore` 예외를 추가한다.

---

## 부록 A. 현재 코드와 규칙의 차이

이 문서의 규칙과 현재 코드가 어긋나는 지점이다. 새 코드는 규칙을 따르고, 아래 항목은 해당 영역을 수정할 때 함께 정리한다.

| 규칙 | 현재 코드 |
|---|---|
| domain 은 다른 계층에 의존하지 않는다 ([2.7](#27-의존-규칙)) | `OrderProcessing`, `ProductStockAllocation`, `ProductRepository`, `PaymentReconciliation` 이 `application.*.dto` 를 import 한다. `PgRequestResult` 는 `infrastructure.payment.pgclient.dto` 를 import 한다. |
| 다른 도메인 엔티티는 ID 로 참조한다 ([5.4](#54-entity-domain)) | `OrderItem → Product`, `ProductLike → User/Product`, `Product → Brand/ProductLikeCount` 가 객체 참조다. |
| 엔티티는 `BaseEntity` 를 상속한다 ([10](#10-영속성)) | `ProductStock`, `ProductLikeCount` 는 상속하지 않아 `created_at` 등이 없다. |
| 생성은 `private` 생성자 + `create()` ([2.8](#28-전술적-설계-규칙)) | `Product` 의 생성자가 `public` 이다. |
| Application Service 는 `@Service`, `Result` 를 반환한다 ([5.2](#52-application-service-application)) | `UserService`, `PointService` 는 `@Component` 다. `OrderService.order()` 는 `Order` 엔티티를 반환한다. |
| `@Transactional` 은 application 계층에서 선언한다 ([9](#9-트랜잭션)) | `OrderProcessing.saveOrder`(domain), `PaymentRecoveryScheduler`(infrastructure)에 선언되어 있다. |
| Request DTO 는 interfaces 에 둔다 ([5.5](#55-dto)) | `UserController.create` 가 `domain.user.dto.command.UserCreateInfo` 를 요청 본문으로 직접 받는다. `OrderCreateRequest` 가 `application` 의 `OrderLineItem` 을 필드로 쓴다. |
| Bean Validation 은 `@Valid` 와 함께 쓴다 ([8](#8-검증validation)) | `PointChargeRequest` 에 `@NotNull` 이 있으나 `PointController.charge` 에 `@Valid` 가 없다. |
| 조회 실패는 `EntityNotFoundException` → 404 ([7.3](#73-어떤-예외를-던지는가)) | `OrderService.getOrderStatus`, `ProductLikeService` 등은 `IllegalArgumentException` 을 던져 400 으로 응답한다. |
| 모든 캐시 키에 TTL 을 설정한다 ([12.1](#121-redis)) | `products` 캐시는 별도 `CacheManager` 설정이 없어 TTL 이 없고, 상품 변경 시 evict 도 없다. |
| 설정은 `@ConfigurationProperties` record 로 바인딩한다 ([14.2](#142-규칙)) | `PaymentService`, `FeignClientConfig`, `Kafka*MessagePublisher` 가 `@Value` 를 쓴다. `PgSimulatorFeignClient` 의 URL(`http://localhost:8082`)이 코드에 고정되어 있다. |
| 이벤트 리스너는 `@TransactionalEventListener` 로 구독한다 ([2.9](#29-이벤트와-메시지)) | `ProductEventListener.handleProductView` 에 리스너 어노테이션이 없어 `ProductViewedEvent` 가 소비되지 않는다. `handleProductUnlike` 는 `ProductUnlikedEvent` 가 아닌 `ProductLikedEvent` 를 받는다. |
| 메시지 타입과 컨슈머 분기를 맞춘다 ([12.2](#122-kafka)) | commerce-streamer 의 `productListener` 가 `eventType` 이 아닌 `aggregateType` 으로 분기한다. 이미 처리한 메시지는 `acknowledge()` 없이 반환한다. |
| 테스트는 실제 동작을 검증한다 ([15](#15-테스트)) | `ProductLikeServiceTest` 는 act 구문이 주석 처리되어 있다. |
| 자동 검증 | CI 워크플로, DB 마이그레이션 도구, Outbox/DLT 가 없다. 계층 의존은 ArchUnit 대신 Checkstyle `ImportControl` 로 검사하며, 위 domain 위반 7개 파일은 허용 목록에 있다. |

---
