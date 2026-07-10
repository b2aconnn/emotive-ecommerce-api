# 사용자별 주문 통계 배치 (일별/주별) 설계

## 배경 및 목적

운영자가 사용자별로 "얼마나 주문했는지"를 일별/주별로 파악할 수 있는 리포트가 필요하다. 단순 주문 건수/금액/수량뿐 아니라, 그 기간에 사용자가 주문한 상품들이 얼마나 인기 있는지(좋아요 수 합계)와, 그 사용자 본인이 같은 기간에 좋아요를 얼마나 눌렀는지도 함께 보고 싶다.

이 리포트는 운영자/관리자 전용이며, 실시간 조회가 아니라 배치로 미리 집계 테이블에 적재하는 방식으로 만든다.

## 범위

- 대상: `apps/commerce-api`. 신규 도메인 `orderstat`을 추가한다.
- 포함: 일별/주별 배치 집계, 집계 결과 조회 API(전체 유저 목록), 관련 기존 코드 이슈(스케줄링 비활성화) 대응.
- 제외(Out of scope): 특정 유저 단건 히스토리 조회 API, 월별 이상 다른 주기, 실시간 집계, 좋아요 취소 이력의 완전한 보존(스냅샷 방식의 한계 수용).

## 아키텍처

`commerce-api-conventions`의 계층 구조를 그대로 따른다.

```
interfaces/api/orderstat/     UserOrderStatV1Controller, ApiSpec, Dto
        ↓
application/orderstat/        UserOrderStatAppService (배치 오케스트레이션 + 조회)
        ↓
domain/orderstat/             UserOrderStat(Entity), StatPeriodType(Enum), UserOrderStatRepository(인터페이스)
        ↑ 구현
infrastructure/orderstat/     UserOrderStatRepositoryImpl(QueryDSL), 스케줄러
```

집계에 필요한 원본 데이터(주문/주문상품/좋아요/좋아요수)는 기존 `domain/order`, `domain/productlike` 도메인의 Repository 인터페이스에 조회용 메서드를 추가해서 읽는다. `orderstat` 도메인은 이 두 도메인을 조합하는 애플리케이션 계층(`UserOrderStatAppService`)에서만 교차 참조하고, `domain/orderstat` 자체는 다른 도메인 타입을 알지 못한다(의존 방향 규칙 준수).

배치는 `infrastructure/orderstat/scheduler/UserOrderStatScheduler`에 `@Scheduled` 메서드 두 개(`generateDailyStat`, `generateWeeklyStat`)로 구현하고, 각각 `UserOrderStatAppService`의 배치 메서드를 호출한다. 기존 `PaymentRecoveryScheduler`와 동일한 컨벤션(try/catch + 로깅, `@Transactional`)을 따른다.

## 기존 코드 이슈 대응: 스케줄링 활성화

`CommerceApiApplication`의 `@EnableScheduling`이 현재 주석 처리되어 있어 유일한 기존 스케줄러(`PaymentRecoveryScheduler`, 10분마다 결제 상태 복구)가 동작하지 않는 상태다. 이번 통계 배치를 동작시키려면 `@EnableScheduling`을 활성화해야 하는데, 그러면 그동안 꺼져 있던 결제 복구 스케줄러도 함께 켜진다.

이를 피하기 위해:
- `CommerceApiApplication`에서 `@EnableScheduling` 주석을 해제한다.
- `PaymentRecoveryScheduler`에 `@ConditionalOnProperty(value = "scheduler.payment-recovery.enabled", havingValue = "true", matchIfMissing = false)`를 추가해 기본값으로는 계속 비활성 상태를 유지한다(운영진이 준비되면 프로퍼티로 켤 수 있음).
- 새 `UserOrderStatScheduler`는 별도 플래그 없이 기본 활성 상태로 둔다(이번 기능의 핵심이므로).

## 데이터 모델

`domain/orderstat/UserOrderStat` 엔티티, 테이블명 `user_order_stat`:

| 필드 | 타입 | 설명 |
|---|---|---|
| userId | Long | 대상 사용자 |
| periodType | StatPeriodType(DAILY/WEEKLY) | 집계 주기 |
| periodStart | LocalDate | DAILY는 해당 일자, WEEKLY는 ISO 주의 월요일 |
| orderCount | Long | 기간 내 COMPLETED 주문 건수 |
| totalAmount | Long | 기간 내 COMPLETED 주문의 `Order.totalAmount` 합 |
| totalQuantity | Long | 기간 내 COMPLETED 주문에 포함된 주문상품 수량 합 |
| orderedProductsLikeSum | Long | 기간 내 주문한 상품(중복 제거) 각각의 현재 `ProductLikeCount.likeCount` 합 |
| myLikeCount | Long | 기간 내 그 사용자가 새로 누른 좋아요(`product_like` 생성) 건수. 상품 종류 무관, 집계 시점 스냅샷 |

`(userId, periodType, periodStart)`에 유니크 제약을 두고, 배치가 같은 기간에 재실행되어도 upsert(존재하면 갱신, 없으면 생성)로 멱등하게 동작한다.

## 집계 로직

`UserOrderStatAppService.generateDailyStat(LocalDate targetDate)` / `generateWeeklyStat(LocalDate weekMonday)`가 다음을 수행한다 (기간 `[start, end)`는 각각 D-1 하루, 지난 ISO 주 월~일):

1. **주문 집계 (대상 유저 확정)**: `OrderRepository`에 추가되는 조회로, 기간 내 `status = COMPLETED`인 주문을 `userId`로 그룹핑해 `orderCount`, `totalAmount` 합, `totalQuantity`(연관 `order_item.quantity` 합)를 구한다. 이 결과에 포함된 userId 집합이 이번 리포트의 대상 유저 전체다. 이 단계에서 주문이 없는 유저는 이후 단계와 무관하게 리포트에 나타나지 않는다.
2. **주문 상품 좋아요 합산**: 같은 기간·같은 유저의 주문상품에서 (userId, productId) distinct 쌍을 조회하고, 유저별로 productId 목록을 모은다. `ProductLikeCountRepository`에 `findByProductIdIn(List<Long>)` 형태의 벌크 조회를 추가해 필요한 상품들의 현재 좋아요 수를 한 번에 가져온 뒤, 유저별 productId 목록에 대해 합산해 `orderedProductsLikeSum`을 만든다.
3. **본인 좋아요 카운트**: `ProductLikeRepository`(또는 신규 조회 인터페이스)에 기간 내 `product_like.created_at` 기준으로 `userId`별 생성 건수를 세는 조회를 추가해 `myLikeCount`를 만든다. 1단계 대상 유저 집합에 없는 유저의 좋아요는 버린다.
4. 1~3의 결과를 유저별로 합쳐 `UserOrderStat`을 upsert한다.

## API

`GET /api/v1/admin/order-stats?periodType=DAILY&periodStart=2026-07-10` (페이징 파라미터는 기존 목록 조회 컨벤션을 따름)

응답: 해당 `periodType`/`periodStart`에 속하는 전체 유저의 `UserOrderStatV1Dto.Response` 리스트(userId, orderCount, totalAmount, totalQuantity, orderedProductsLikeSum, myLikeCount). 특정 유저 단건 조회, periodType 외 다른 조건 필터는 이번 범위에서 다루지 않는다.

## 에러 처리 및 운영 고려사항

- 배치 메서드는 `PaymentRecoveryScheduler`와 동일하게 전체를 try/catch로 감싸고 실패를 로깅한다. 한 유저의 집계 실패가 전체 배치를 막지 않도록, 유저 단위가 아니라 쿼리 단위로 묶어서 처리하되(집계는 애초에 벌크 쿼리 기반이라 유저 단위 실패라는 개념 자체가 크지 않음), 배치 전체가 실패하면 다음 스케줄 사이클(또는 수동 재실행)에서 동일 기간에 대해 재실행되어도 upsert로 안전하게 덮어써진다.
- 스케줄 실행 시각과 무관하게 항상 "완결된 과거 기간"만 집계하므로(D-1, 지난 주), 배치 시점에 아직 진행 중인 당일/이번 주 데이터를 부분 집계하는 문제는 없다.
- `product_like`가 하드 삭제되므로, 같은 집계 기간 안에서 좋아요→취소가 모두 일어난 건은 배치 실행 시점에 이미 사라져 `myLikeCount`에서 빠질 수 있다. 이는 설계상 수용하기로 한 한계다.

## 테스트 전략

- **도메인**: `UserOrderStat` 생성 팩토리, upsert(갱신) 로직에 대한 단위 테스트.
- **애플리케이션**: `UserOrderStatAppService`의 배치 생성 로직에 대한 통합 테스트 — 주문 집계(금액/수량/건수), 상품 좋아요 합산(중복 상품 제거 확인), 본인 좋아요 카운트, 주문이 없는 유저가 리포트에서 제외되는 케이스, 재실행 시 upsert로 값이 갱신되는 케이스를 포함한다.
- **API**: `UserOrderStatV1ApiE2ETest`로 조회 응답 스키마와 필터링(periodType/periodStart)을 검증한다.
- **기존 코드 영향**: `PaymentRecoveryScheduler`에 프로퍼티 게이트가 추가되므로, 프로퍼티 미설정 시 여전히 비활성 상태인지 확인하는 테스트(또는 최소한 기존 테스트가 깨지지 않는지 확인)를 포함한다.
