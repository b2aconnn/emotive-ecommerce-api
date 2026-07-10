# k6 성능 테스트 시나리오 설계

## 배경 및 목적

`load-test/` 디렉토리에 최소 골격(browsing/point/purchase 3개 시나리오, 단일 하드코딩 유저/상품)이 이미 존재한다. 이를 "실제 서비스 환경을 가정한" 시나리오로 확장한다.

목적은 두 가지다.
1. **용량 산정 / 배포 전 검증**: 기대 트래픽에서 SLA(응답시간/에러율)를 충족하는지 확인하고, 점진적으로 부하를 올려 시스템 한계 지점을 찾는다.
2. **특정 병목 지점 진단**: 주문/결제 동시성, 재고 오버셀, 포인트 충전 락 경합 등 동시성 이슈를 의도적으로 재현한다.

로컬(docker-compose)과 스테이징 환경 양쪽에서 `-e BASE_URL=`로 대상을 전환할 수 있어야 한다.

## 선행 조건: 백엔드 하드코딩 수정

`PointV1Controller`(`apps/commerce-api/.../interfaces/api/point/PointV1Controller.java`)의 `charge`, `get` 두 메서드는 `@CurrentUser` 파라미터가 주석 처리되어 있고 `userId = "user1234"`로 하드코딩되어 있다. `X-USER-ID` 헤더로 어떤 값을 보내도 항상 회원 #1(`user1234`)의 포인트 row에 반영된다.

k6에서 VU별로 서로 다른 유저의 포인트 흐름을 검증하려면 이 하드코딩을 먼저 제거해야 한다. `CurrentUserArgumentResolver`(`support/resolver/CurrentUserArgumentResolver.java`)는 이미 `@CurrentUser` 애노테이션이 붙은 `User` 타입 파라미터를 `X-USER-ID` 헤더로 조회해 주입하도록 구현·등록(`WebConfig`)까지 완료되어 있다 — 아직 아무 컨트롤러도 쓰고 있지 않을 뿐이다.

**수정 범위** (`commerce-api-conventions`의 interfaces 계층 컨벤션을 따름):
- `PointV1Controller`: `charge`, `get` 메서드에 `@CurrentUser User currentUser` 파라미터를 추가하고, `pointAppService.charge(currentUser.getUserId(), ...)` / `pointAppService.get(currentUser.getUserId())`로 교체. 하드코딩된 `String userId = "user1234"` 제거.
- `PointV1ApiSpec`: 인터페이스 메서드 시그니처를 Controller와 동일하게 맞춘다(`@Override` 매칭을 위해 파라미터 개수가 일치해야 함). `currentUser` 파라미터는 요청 바디/쿼리가 아니라 헤더에서 오므로 `@Parameter(hidden = true)`로 Swagger 문서에서 숨긴다.
- `application/point/PointAppService`, `PointV1Dto`는 변경 없음(이미 `String userId`를 받는 시그니처).

**영향받는 기존 테스트**: `PointV1ApiE2ETest`는 이미 모든 케이스에서 `X-USER-ID: user1234`를 보내고 있어(우연히 하드코딩 값과 일치) 이 수정으로 깨지지 않는다. 유저 존재 검증(404/400)은 이미 `UserAuthenticationInterceptor`가 컨트롤러 진입 전에 처리하고 있으므로 그 부분도 영향 없음.

이 수정은 k6 스크립트가 아니라 `apps/commerce-api` 프로덕션 코드 변경이므로, 별도 커밋으로 분리한다.

## k6 디렉토리 구조

```
load-test/
  config.js                    # BASE_URL(-e로 오버라이드), 방문자 유형 비율, think-time 범위,
                                # 프로파일별 stage/threshold 프리셋
  seed.sql                     # member 300명(+point 잔액 선충전), 상품 50개(브랜드 4~5개),
                                # 핫아이템 1개(재고 50개)
  helpers/
    api.js                     # HTTP 호출 함수 (X-USER-ID 헤더 지원 추가)
    pool.js                    # __VU 기반 유저/상품 결정론적 선택, 랜덤 think-time
  scenarios/
    user-journey.js            # 통합 일상 트래픽 시나리오
    hotitem-flow.js            # 재고 적은 단일 상품 동시 주문 (spike 전용)
    point-charge-event.js      # 포인트 충전 몰림 이벤트 (spike 전용, 기존 point-flow.js 대체)
  smoke.js                     # entrypoint
  load.js                      # entrypoint
  stress.js                    # entrypoint
  spike.js                     # entrypoint
  README.md                    # 실행법, BASE_URL 오버라이드, 재고 오버셀 수동 검증 SQL
```

기존 `main.js`, `scenarios/browsing-flow.js`, `scenarios/point-flow.js`, `scenarios/purchase-flow.js`는 `user-journey.js`로 통합되며 제거한다.

## 시드 데이터 (`seed.sql`)

- **일반 유저 풀**: `member` 300명. `user_id` = `loadtest0001` ~ `loadtest0300`. TRUNCATE 후 순차 삽입이므로 PK(`member.id`)가 1~300으로 부여되고, 주문 API가 요구하는 숫자 `userId`로 그대로 쓸 수 있다. 각 유저에 `point.balance` 100,000 선충전(주문 시 포인트 부족으로 실패하지 않도록).
- **일반 상품 풀**: 브랜드 4~5개에 걸쳐 상품 50개, 재고 100,000개(품절로 인한 실패가 섞이지 않도록 충분히 크게).
- **핫아이템**: 별도 상품 1개, 재고 50개. spike 시나리오 전용 — 재고 오버셀 재현이 목적이므로 의도적으로 적게 설정.
- **포인트 동시성 계정**: `user1234`(회원 #1)를 유지하되, 위 하드코딩 수정 이후에는 `point-charge-event.js`가 이 계정 하나에 의도적으로 동시 충전 요청을 몰아 락 경합을 재현하는 용도로 명시적으로 사용한다(다른 시나리오는 각자 고유 계정 사용).

## 시나리오

### `user-journey.js` (핵심 시나리오, load/stress에서 사용)

1 VU = 1 방문자 세션.

1. 상품 목록 조회 → 관심 상품 1~3개 상세 조회(랜덤 개수, think-time 1~3초)
2. 상품의 브랜드 조회
3. 방문자 유형 분기(누적 확률):
   - **70% 이탈**: 세션 종료
   - **20% 관심**: 포인트 잔액 조회만 하고 이탈
   - **10% 구매 완주**: 포인트 잔액 조회 → 1단계에서 조회했던 상품 중 마지막으로 본 상품을 구매 대상으로 정하고 그 상세 응답의 `price`로 주문 총액을 계산 → 잔액이 부족할 때만 충전 → 해당 상품으로 주문 생성(보유 포인트 일부 사용) → 결제 요청 → 주문 상태 확인

각 VU는 `pool.js`의 `pickUser(__VU)`로 유저 풀에서 결정론적으로 배정된 계정을 사용한다(같은 VU는 항상 같은 계정 → 세션 내 일관성 보장).

`smoke.js`에서 세 분기를 모두 강제로 실행해야 하므로, `user-journey.js`는 분기 로직을 별도 함수(예: `runBrowseOnly`, `runCheckPoint`, `runFullPurchase`)로 분리하고 기본 export는 확률에 따라 이 중 하나를 호출하는 얇은 래퍼로 둔다. `smoke.js`는 세 함수를 직접 import해서 순서대로 호출한다.

### `hotitem-flow.js` (spike 전용)

`pickUser`로 임의 유저를 뽑아, 재고 50개인 핫아이템 상품 하나에 대해 바로 주문 생성 → 결제 요청까지 수행한다. 브라우징 단계 없이 곧바로 구매를 시도해 몰림 상황을 재현한다.

### `point-charge-event.js` (spike 전용)

`user1234` 계정에 대해 반복적으로 `POST /api/v1/points/charge`를 호출해 동시 락 경합을 재현한다(기존 `point-flow.js`를 대체, 의미를 "이벤트성 프로모션 충전 몰림"으로 명확히 함).

## 프로파일별 실행 정의

| 프로파일 | 시나리오 | 부하 패턴 | 검증 방식 |
|---|---|---|---|
| `smoke.js` | `user-journey`의 3개 분기(이탈/관심/구매완주)를 각 1회씩 강제 실행 + `hotitem-flow` 1회 + `point-charge-event` 1회 | VU 1, iteration 고정(랜덤 분기에 의존하지 않음) | 하드 threshold: p95<500ms, p99<1000ms, 실패율<1% |
| `load.js` | `user-journey`만(내부 70/20/10 랜덤 분기) | ramping-vus: 0→100(1m) → 100 유지(5m) → 0(1m) | p95<500ms, p99<1000ms, 실패율<1% |
| `stress.js` | `user-journey`만 | 계단식 ramping-vus: 0→50→100→150→200, 구간별 1~2분 유지 후 다음 단계, 마지막 램프다운 | 하드 threshold로 강제 실패시키지 않음(관찰용). 구간별 p95/실패율을 리포트에 기록해 어느 지점에서 SLA가 무너지는지 확인 |
| `spike.js` | `hotitem-flow` + `point-charge-event` | 각각 독립된 `ramping-arrival-rate` executor 2개(시나리오 키 분리, VU 풀 겹치지 않음): 0→300 req/s를 10~15초 내 급증 → 30초 유지 → 급감. `preAllocatedVUs`/`maxVUs`를 요청 레이트를 감당할 만큼 넉넉히(예: 300/500) 설정 | 하드 threshold 없음. 커스텀 `Counter`로 "재고부족" 응답 비율 집계. 테스트 종료 후 `README.md`에 첨부된 검증 SQL(`SELECT quantity FROM product_stock WHERE product_id = <핫아이템ID>`와 실제 생성된 주문 수 비교)로 오버셀 여부를 수동 확인 |

## Out of scope

- 상품 좋아요(product-like), 쿠폰 API는 현재 공개 HTTP 엔드포인트가 없어 시나리오에 포함하지 않는다.
- `soak`(장시간 유지) 프로파일은 이번 범위에서 제외한다(메모리 누수 등은 별도 요청 시 추가).
- k6 결과를 Grafana/InfluxDB 등 외부 대시보드로 내보내는 설정은 포함하지 않는다(기본 콘솔/summary 출력만).
