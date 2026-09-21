/**
 * commerce-api 부하 테스트 — 유저 워크플로우 가중 시나리오
 *
 * 세션 페르소나 비중 (세션 시작 100 기준, 가설):
 *   탐색형(browse_and_bounce)        45%
 *   브랜드 탐색형(brand_first)       15%
 *   구매 전환형(converter)           25%
 *   재방문/포인트 관리형(point_manager) 10%
 *   신규 가입형(new_signup)           5%
 *
 * 이 비중대로 각 시나리오의 VU 수를 나누고, 각 페르소나 내부에서는
 * 실제 유저 흐름(목록→상세→포인트 확인→주문→결제→상태 폴링 등)을 그대로 재현한다.
 * 결과적으로 개별 엔드포인트 호출 비중은 대략 GET /products 38% · GET /products/{id} 25% ·
 * GET /points 8% · GET /orders/{id}/status 7.5% · POST /orders 5% · POST /payments/order-payment 5% ·
 * GET /brands 3.4% · GET /brands/{id} 3.4% · GET /users/{id} 2.3% · POST /points/charge 1.4% ·
 * POST /users 1.1% 로 수렴하도록 설계했다.
 *
 * 기존 k6 스크립트는 참고하지 않고 새로 작성함. 실행 전 아래 "실행 전 준비" 섹션을 반드시 확인할 것.
 *
 * ────────────────────────────── 실행 전 준비 ──────────────────────────────
 * 1) X-USER-ID 인터셉터는 실제 존재하는 member.user_id 값만 통과시킨다.
 *    또한 주문 생성 바디의 userId는 member.user_id(문자열, 로그인 ID)가 아니라
 *    member.id(PK, 숫자)를 그대로 요구한다 — 즉 로그인 헤더용 ID와 주문 바디용 ID는
 *    서로 다른 값이다. 아래 SQL로 실제 값을 뽑아 SEED_USERS_JSON 으로 주입할 것:
 *
 *      SELECT id AS memberId, user_id AS loginId FROM member LIMIT 200;
 *
 *    k6 실행 시: -e SEED_USERS_JSON='[{"memberId":1,"loginId":"abcd1234"}, ...]'
 *
 * 2) PRODUCT_ID_MIN/MAX, BRAND_ID_MIN/MAX 는 실제 product/brand 테이블의 id 범위로
 *    맞출 것 (기본값은 1~30 / 1~8 — seed 데이터와 다르면 404가 대량 발생한다).
 *
 * 3) /api/points 의 GET/POST 는 서버 코드가 X-USER-ID를 무시하고 userId를
 *    "user1234"로 하드코딩하고 있다(PointController 확인됨). 즉 이 스크립트가 보내는
 *    포인트 관련 두 엔드포인트는 어떤 로그인 ID를 쓰든 실제로는 전부 동일 레코드에
 *    몰린다 — 의도된 동작이 아니라 알려진 구현 갭이므로, 포인트 API의 "유저별 부하"를
 *    검증하려면 코드 수정이 선행되어야 한다. 이 스크립트는 현재 서버 동작 그대로 부하만 생성한다.
 * ───────────────────────────────────────────────────────────────────────
 */

import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Counter } from 'k6/metrics';

// ───────────────────────────── 설정 ─────────────────────────────

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const PEAK_VUS = Number(__ENV.PEAK_VUS || 100);
const RAMP_UP = __ENV.RAMP_UP || '30s';
const STEADY = __ENV.STEADY || '3m';
const RAMP_DOWN = __ENV.RAMP_DOWN || '30s';

const PRODUCT_ID_MIN = Number(__ENV.PRODUCT_ID_MIN || 1);
const PRODUCT_ID_MAX = Number(__ENV.PRODUCT_ID_MAX || 30);
const BRAND_ID_MIN = Number(__ENV.BRAND_ID_MIN || 1);
const BRAND_ID_MAX = Number(__ENV.BRAND_ID_MAX || 8);

// TODO: 실제 member 테이블 값으로 교체 (상단 "실행 전 준비" 1번 참고)
const DEFAULT_SEED_USERS = [
  { memberId: 1, loginId: 'k6user01' },
  { memberId: 2, loginId: 'k6user02' },
  { memberId: 3, loginId: 'k6user03' },
  { memberId: 4, loginId: 'k6user04' },
  { memberId: 5, loginId: 'k6user05' },
  { memberId: 6, loginId: 'k6user06' },
  { memberId: 7, loginId: 'k6user07' },
  { memberId: 8, loginId: 'k6user08' },
  { memberId: 9, loginId: 'k6user09' },
  { memberId: 10, loginId: 'k6user10' },
];
const SEED_USERS = __ENV.SEED_USERS_JSON
  ? JSON.parse(__ENV.SEED_USERS_JSON)
  : DEFAULT_SEED_USERS;

const CARD_TYPES = ['SAMSUNG', 'KB', 'HYUNDAI'];
const SORT_TYPES = ['LASTEST', 'PRICE_ASC', 'LIKES_DESC'];

// ───────────────────────────── 커스텀 메트릭 ─────────────────────────────

const orderCreated = new Counter('order_created_total');
const orderFailed = new Counter('order_failed_total');
const paymentRequested = new Counter('payment_requested_total');
const signupCreated = new Counter('signup_created_total');

// ───────────────────────────── 헬퍼 ─────────────────────────────

function randomIntBetween(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function randomItem(arr) {
  return arr[randomIntBetween(0, arr.length - 1)];
}

function pickUser() {
  return randomItem(SEED_USERS);
}

function pickProductId() {
  return randomIntBetween(PRODUCT_ID_MIN, PRODUCT_ID_MAX);
}

function pickBrandId() {
  return randomIntBetween(BRAND_ID_MIN, BRAND_ID_MAX);
}

function headers(loginId, endpointTag) {
  return {
    headers: { 'X-USER-ID': loginId, 'Content-Type': 'application/json' },
    tags: { endpoint: endpointTag },
  };
}

// 사용자 사이 think time (읽기 위주 탐색은 짧게, 폼 입력이 필요한 구간은 길게)
function thinkShort() {
  sleep(randomIntBetween(1, 3));
}
function thinkLong() {
  sleep(randomIntBetween(2, 5));
}

// ───────────────────────────── 페르소나 1: 탐색형 (45%) ─────────────────────────────
// GET /products → (60%) GET /products/{id}

export function browsePersona() {
  const user = pickUser();

  group('탐색형: 상품 목록 조회', () => {
    const res = http.get(
      `${BASE_URL}/api/products?offset=0&size=20&sortBy=${randomItem(SORT_TYPES)}`,
      headers(user.loginId, 'GET /api/products')
    );
    check(res, { '목록 조회 200': (r) => r.status === 200 });
    thinkShort();
  });

  if (Math.random() < 0.6) {
    group('탐색형: 상품 상세 조회', () => {
      const productId = pickProductId();
      const res = http.get(
        `${BASE_URL}/api/products/${productId}`,
        headers(user.loginId, 'GET /api/products/{id}')
      );
      check(res, { '상세 조회 200/404': (r) => r.status === 200 || r.status === 404 });
      thinkShort();
    });
  }
}

// ───────────────────────────── 페르소나 2: 브랜드 탐색형 (15%) ─────────────────────────────
// GET /brands → GET /brands/{id} → GET /products?brandId= → GET /products/{id}

export function brandFirstPersona() {
  const user = pickUser();

  group('브랜드 탐색형: 브랜드 목록', () => {
    const res = http.get(
      `${BASE_URL}/api/brands?offset=0&size=20`,
      headers(user.loginId, 'GET /api/brands')
    );
    check(res, { '브랜드 목록 200': (r) => r.status === 200 });
    thinkShort();
  });

  const brandId = pickBrandId();

  group('브랜드 탐색형: 브랜드 상세', () => {
    const res = http.get(
      `${BASE_URL}/api/brands/${brandId}`,
      headers(user.loginId, 'GET /api/brands/{id}')
    );
    check(res, { '브랜드 상세 200/404': (r) => r.status === 200 || r.status === 404 });
    thinkShort();
  });

  group('브랜드 탐색형: 브랜드 필터 상품 목록', () => {
    const res = http.get(
      `${BASE_URL}/api/products?brandId=${brandId}&offset=0&size=20`,
      headers(user.loginId, 'GET /api/products')
    );
    check(res, { '브랜드 필터 목록 200': (r) => r.status === 200 });
    thinkShort();
  });

  group('브랜드 탐색형: 상품 상세', () => {
    const productId = pickProductId();
    const res = http.get(
      `${BASE_URL}/api/products/${productId}`,
      headers(user.loginId, 'GET /api/products/{id}')
    );
    check(res, { '상세 조회 200/404': (r) => r.status === 200 || r.status === 404 });
    thinkShort();
  });
}

// ───────────────────────────── 페르소나 3: 구매 전환형 (25%) ─────────────────────────────
// GET /products → GET /products/{id} → GET /points → (8%) POST /points/charge
// → POST /orders → POST /payments/order-payment → GET /orders/{id}/status (1~2회 폴링)
// ※ POST /payments/callback 은 PG(pg-simulator)가 비동기로 트리거하는 시스템 이벤트이므로
//   이 VU 흐름에서 직접 호출하지 않는다 (문서 04절 참고).

export function converterPersona() {
  const user = pickUser();

  group('구매 전환형: 상품 탐색', () => {
    const res = http.get(
      `${BASE_URL}/api/products?offset=0&size=20`,
      headers(user.loginId, 'GET /api/products')
    );
    check(res, { '목록 조회 200': (r) => r.status === 200 });
    thinkShort();
  });

  const productId = pickProductId();

  group('구매 전환형: 상품 상세', () => {
    const res = http.get(
      `${BASE_URL}/api/products/${productId}`,
      headers(user.loginId, 'GET /api/products/{id}')
    );
    check(res, { '상세 조회 200/404': (r) => r.status === 200 || r.status === 404 });
    thinkShort();
  });

  group('구매 전환형: 포인트 잔액 확인', () => {
    const res = http.get(`${BASE_URL}/api/points`, headers(user.loginId, 'GET /api/points'));
    check(res, { '포인트 조회 200': (r) => r.status === 200 });
  });

  if (Math.random() < 0.08) {
    group('구매 전환형: 포인트 충전', () => {
      const res = http.post(
        `${BASE_URL}/api/points/charge`,
        JSON.stringify({ amount: randomItem([5000, 10000, 30000, 50000]) }),
        headers(user.loginId, 'POST /api/points/charge')
      );
      check(res, { '충전 200': (r) => r.status === 200 });
    });
  }

  thinkLong();

  let orderId = null;

  group('구매 전환형: 주문 생성', () => {
    const body = JSON.stringify({
      userId: user.memberId,
      orderer: `k6-load-${user.memberId}`,
      deliveryAddress: '서울시 강남구 테헤란로 1',
      contactNumber: '01000000000',
      items: [{ productId, quantity: randomIntBetween(1, 2) }],
      availablePoints: 0,
      couponId: null,
      paymentMethod: 'CARD',
      cardType: randomItem(CARD_TYPES),
      cardNo: '1234-5678-9012-3456',
    });
    const res = http.post(`${BASE_URL}/api/orders`, body, headers(user.loginId, 'POST /api/orders'));
    const ok = check(res, { '주문 생성 200': (r) => r.status === 200 });
    if (ok) {
      orderCreated.add(1);
      try {
        orderId = res.json('data.orderId');
      } catch (e) {
        orderId = null;
      }
    } else {
      orderFailed.add(1);
    }
  });

  if (orderId) {
    group('구매 전환형: 결제 요청', () => {
      const body = JSON.stringify({
        orderId,
        paymentMethod: 'CARD',
        cardType: randomItem(CARD_TYPES),
        cardNo: '1234-5678-9012-3456',
      });
      const res = http.post(
        `${BASE_URL}/api/payments/order-payment`,
        body,
        headers(user.loginId, 'POST /api/payments/order-payment')
      );
      check(res, { '결제 요청 200': (r) => r.status === 200 });
      paymentRequested.add(1);
    });

    const pollCount = randomIntBetween(1, 2);
    for (let i = 0; i < pollCount; i++) {
      sleep(randomIntBetween(1, 2));
      group('구매 전환형: 주문 상태 폴링', () => {
        const res = http.get(
          `${BASE_URL}/api/orders/${orderId}/status`,
          headers(user.loginId, 'GET /api/orders/{id}/status')
        );
        check(res, { '상태 조회 200': (r) => r.status === 200 });
      });
    }
  }
}

// ───────────────────────────── 페르소나 4: 재방문/포인트 관리형 (10%) ─────────────────────────────
// GET /users/{id} → GET /points → (40%) POST /points/charge

export function pointManagerPersona() {
  const user = pickUser();

  group('재방문형: 프로필 조회', () => {
    const res = http.get(
      `${BASE_URL}/api/users/${user.loginId}`,
      headers(user.loginId, 'GET /api/users/{id}')
    );
    check(res, { '프로필 조회 200/404': (r) => r.status === 200 || r.status === 404 });
    thinkShort();
  });

  group('재방문형: 포인트 잔액 확인', () => {
    const res = http.get(`${BASE_URL}/api/points`, headers(user.loginId, 'GET /api/points'));
    check(res, { '포인트 조회 200': (r) => r.status === 200 });
  });

  if (Math.random() < 0.4) {
    thinkShort();
    group('재방문형: 포인트 충전', () => {
      const res = http.post(
        `${BASE_URL}/api/points/charge`,
        JSON.stringify({ amount: randomItem([10000, 20000, 50000]) }),
        headers(user.loginId, 'POST /api/points/charge')
      );
      check(res, { '충전 200': (r) => r.status === 200 });
    });
  }
}

// ───────────────────────────── 페르소나 5: 신규 가입형 (5%) ─────────────────────────────
// POST /users → GET /products
// ※ 회원가입 엔드포인트도 전역 인터셉터에 걸려 X-USER-ID가 필요하다(구현 갭, 문서 03절 참고).
//   실제로 아직 존재하지 않는 사용자이므로, 기존 시드 유저의 헤더를 빌려 호출한다.

export function newSignupPersona() {
  const borrowedAuth = pickUser();
  const suffix = `${__VU}${__ITER}`.slice(-6);
  const newLoginId = `k6new${suffix}`.slice(0, 10);

  group('신규 가입형: 회원가입', () => {
    const body = JSON.stringify({
      userId: newLoginId,
      name: `로드테스트유저${suffix}`,
      email: `k6load${suffix}@example.com`,
      birthDateString: '1995-05-05',
      gender: randomItem(['MALE', 'FEMALE']),
    });
    const res = http.post(
      `${BASE_URL}/api/users`,
      body,
      headers(borrowedAuth.loginId, 'POST /api/users')
    );
    const ok = check(res, { '가입 200': (r) => r.status === 200 });
    if (ok) signupCreated.add(1);
  });

  thinkShort();

  group('신규 가입형: 첫 탐색', () => {
    const res = http.get(
      `${BASE_URL}/api/products?offset=0&size=20`,
      headers(borrowedAuth.loginId, 'GET /api/products')
    );
    check(res, { '목록 조회 200': (r) => r.status === 200 });
  });
}

// ───────────────────────────── 시나리오 구성 (페르소나 비중대로 VU 배분) ─────────────────────────────

function vusFor(weight) {
  return Math.max(1, Math.round(PEAK_VUS * weight));
}

function stagesFor(weight) {
  return [
    { duration: RAMP_UP, target: vusFor(weight) },
    { duration: STEADY, target: vusFor(weight) },
    { duration: RAMP_DOWN, target: 0 },
  ];
}

export const options = {
  scenarios: {
    browse_and_bounce: {
      executor: 'ramping-vus',
      exec: 'browsePersona',
      startVUs: 0,
      stages: stagesFor(0.45),
      tags: { persona: 'browse_and_bounce' },
    },
    brand_first: {
      executor: 'ramping-vus',
      exec: 'brandFirstPersona',
      startVUs: 0,
      stages: stagesFor(0.15),
      tags: { persona: 'brand_first' },
    },
    converter: {
      executor: 'ramping-vus',
      exec: 'converterPersona',
      startVUs: 0,
      stages: stagesFor(0.25),
      tags: { persona: 'converter' },
    },
    point_manager: {
      executor: 'ramping-vus',
      exec: 'pointManagerPersona',
      startVUs: 0,
      stages: stagesFor(0.10),
      tags: { persona: 'point_manager' },
    },
    new_signup: {
      executor: 'ramping-vus',
      exec: 'newSignupPersona',
      startVUs: 0,
      stages: stagesFor(0.05),
      tags: { persona: 'new_signup' },
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<800', 'p(99)<1000'],
    // 읽기 경로: 목록/상세/브랜드 조회는 더 빡빡한 기준
    'http_req_duration{endpoint:GET /api/products}': ['p(95)<300', 'p(99)<600'],
    'http_req_duration{endpoint:GET /api/products/{id}}': ['p(95)<300', 'p(99)<600'],
    'http_req_duration{endpoint:GET /api/brands}': ['p(95)<300', 'p(99)<600'],
    'http_req_duration{endpoint:GET /api/brands/{id}}': ['p(95)<300', 'p(99)<600'],
    // 쓰기 경로: 재고 비관적 락 · PG 연동이 끼는 구간은 여유를 둠
    'http_req_duration{endpoint:POST /api/orders}': ['p(95)<800', 'p(99)<1500'],
    'http_req_duration{endpoint:POST /api/payments/order-payment}': ['p(95)<1000', 'p(99)<2000'],
  },
};
