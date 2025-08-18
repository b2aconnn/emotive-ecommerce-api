import http from 'k6/http';
import {check, sleep} from 'k6';

export const options = {
    // ext: {
    //     prometheus: {
    //         // Prometheus Remote Write endpoint
    //         push_gateway: {
    //             url: 'http://localhost:9090/api/v1/write',
    //             // 또는 직접적인 remote write 설정
    //         },
    //     },
    // },

    vus: 20, // 동시에 실행할 가상 사용자 수
    duration: '60s', // 테스트 지속 시간
    thresholds: {
        http_req_duration: ['p(95)<500', 'p(99)<500'], // 95% 요청이 500ms 미만이어야 함
        http_req_failed: ['rate<0.01'],   // 실패율 1% 미만
    },
};

export default function () {
    const url = 'http://localhost:8080/api/v1/products?brandId=200&offset=0&size=20';

    const params = {
        headers: {
            'X-USER-ID': 'abcd', // 사용자 ID 값
        },
    };

    const res = http.get(url, params);

    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    sleep(1);
}