// smoke_core_apis.js
import http from 'k6/http';
import {sleep} from 'k6';
import {textSummary} from "https://jslib.k6.io/k6-summary/0.0.2/index.js";

export const options = {
    vus: 3,               // 가벼운 동시 사용자 수
    duration: '20s',      // 20초 정도만
    thresholds: {
        http_req_failed: ['rate<0.05'],        // 전체 실패율 5% 미만
        'http_req_duration{api:smoke}': ['p(95)<4000'], // p95 4초 이내 (dev 환경 고려해서 널널하게)
    },
};

// 환경 변수로 받도록 (CI에서 세팅)
const BASE_URL = __ENV.BASE_URL || 'http://localhost:80';
const AUTH_TOKEN = __ENV.AUTH_TOKEN || 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMDAxNDciLCJyb2xlIjoiVVNFUiIsImlhdCI6MTc2MjQxMjkzNiwiZXhwIjoxNzYzNjIyNTM2fQ.rLVBmoFMQUsGvS3E_W4dN2RAWIJ7o98GBVhSDGoqXO0'; // 필요 없으면 빈 문자열
const GAME_ID = __ENV.GAME_ID || '2';
const MEMBER_ID = __ENV.MEMBER_ID || '2';
const YEAR = __ENV.YEAR || '2025';
const DATE = __ENV.DATE || '2025-05-25';

export default function () {
    const params = {
        headers: AUTH_TOKEN
            ? {
                Authorization: `Bearer ${AUTH_TOKEN}`,
                'Content-Type': 'application/json',
            }
            : {'Content-Type': 'application/json'},
        tags: {
            api: 'smoke',
        },
    };

    // 1) 프로필 조회
    http.get(
        `${BASE_URL}/api/v1/members/${MEMBER_ID}?gameId=${GAME_ID}&after=10&limit=10`,
        params,
    );

    // 2) 내 뱃지 조회
    http.get(
        `${BASE_URL}/api/v1/members/me/badges?gameId=${GAME_ID}&after=10&limit=10`,
        params,
    );

    // 3) 구장별 팬 점유율
    http.get(
        `${BASE_URL}/api/v1/check-ins/stadiums/fan-rates?date=${DATE}`,
        params,
    );

    // 4) 구장별 방문 횟수
    http.get(
        `${BASE_URL}/api/v1/check-ins/stadiums/counts?year=${YEAR}`,
        params,
    );

    // 5) 인증 횟수
    http.get(
        `${BASE_URL}/api/v1/check-ins/counts?year=${YEAR}`,
        params,
    );

    sleep(1);
}

export function handleSummary(data) {
    const webhookUrl = __ENV.DISCORD_WEBHOOK_BE; // GitHub Actions에서 주입

    // 기본 메트릭
    const failedRate = data.metrics.http_req_failed
        ? data.metrics.http_req_failed.values.rate
        : 0;

    const p95 = data.metrics.http_req_duration
        ? data.metrics.http_req_duration.values['p(95)']
        : 0;

    // ✅ 임계값 설정 (밑에서 설명)
    const ERROR_RATE_THRESHOLD = 0.05; // 5%
    const P95_THRESHOLD_MS = 2000;     // 2초

    const isErrorRateBad = failedRate > ERROR_RATE_THRESHOLD;
    const isP95Bad = p95 > P95_THRESHOLD_MS;

    // 디스코드로 보낼 메시지 내용
    if (webhookUrl && (isErrorRateBad || isP95Bad)) {
        const content =
            `⚠️ k6 Smoke Alert (dev)\n` +
            `• http_req_failed: ${(failedRate * 100).toFixed(2)}% (임계값 ${ERROR_RATE_THRESHOLD * 100}% )\n` +
            `• http_req_duration p95: ${p95.toFixed(2)} ms (임계값 ${P95_THRESHOLD_MS} ms)\n` +
            `• 기준 초과 항목: ` +
            `${isErrorRateBad ? 'ErrorRate ' : ''}` +
            `${isP95Bad ? 'p95 ' : ''}`;

        const payload = JSON.stringify({content});

        http.post(webhookUrl, payload, {
            headers: {'Content-Type': 'application/json'},
        });
    }

    // CI에서 summary.json 필요 없다면 빈 객체 리턴
    return {
        stdout: textSummary(data, {indent: " ", enableColors: true}), // 콘솔에 k6 기본 리포트 출력
    };
}
