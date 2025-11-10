// smoke_core_apis.js
import http from 'k6/http';
import {sleep} from 'k6';
import {textSummary} from "https://jslib.k6.io/k6-summary/0.0.2/index.js";

export const options = {
    vus: 3,               // 가벼운 부하 수준 (3명 동시 사용자)
    duration: '20s',      // 약 20초 실행
    thresholds: {
        http_req_failed: ['rate<0.05'],            // 실패율 5% 미만
        'http_req_duration{api:smoke}': ['p(95)<2000'], // p95 2초 미만
    },
};

// === 환경 변수 (GitHub Actions에서 주입) ===
const BASE_URL = __ENV.BASE_URL || 'http://localhost:80';
const AUTH_TOKEN = __ENV.AUTH_TOKEN || '';
const GAME_ID = __ENV.GAME_ID || '2';
const MEMBER_ID = __ENV.MEMBER_ID || '2';
const YEAR = __ENV.YEAR || '2025';
const DATE = __ENV.DATE || '2025-05-25';

export default function () {
    const params = {
        headers: AUTH_TOKEN
            ? {Authorization: `Bearer ${AUTH_TOKEN}`, 'Content-Type': 'application/json'}
            : {'Content-Type': 'application/json'},
        tags: {api: 'smoke'},
    };

    // === 5개 주요 API ===
    http.get(`${BASE_URL}/api/v1/members/${MEMBER_ID}?gameId=${GAME_ID}&after=10&limit=10`, params);
    http.get(`${BASE_URL}/api/v1/members/me/badges?gameId=${GAME_ID}&after=10&limit=10`, params);
    http.get(`${BASE_URL}/api/v1/check-ins/stadiums/fan-rates?date=${DATE}`, params);
    http.get(`${BASE_URL}/api/v1/check-ins/stadiums/counts?year=${YEAR}`, params);
    http.get(`${BASE_URL}/api/v1/check-ins/counts?year=${YEAR}`, params);

    sleep(1);
}

// === Discord 알림 로직 ===
export function handleSummary(data) {
    const webhookUrl = __ENV.DISCORD_WEBHOOK_BE; // GitHub Actions에서 설정
    const ENV_NAME = __ENV.ENV_NAME || 'smoke-ci';

    // === 메트릭 계산 ===
    const failedRate = data.metrics.http_req_failed
        ? data.metrics.http_req_failed.values.rate
        : 0;

    const p95 = data.metrics.http_req_duration
        ? data.metrics.http_req_duration.values['p(95)']
        : 0;

    // === 임계값 (운영용 2초 기준) ===
    const ERROR_RATE_THRESHOLD = 0.05;  // 5%
    const P95_THRESHOLD_MS = 2000;      // 2초

    const isErrorRateBad = failedRate > ERROR_RATE_THRESHOLD;
    const isP95Bad = p95 > P95_THRESHOLD_MS;

    // === Discord 알림 ===
    if (webhookUrl && (isErrorRateBad || isP95Bad)) {
        const content =
            `⚠️ **k6 Smoke Alert (${ENV_NAME})**\n` +
            `• ❌ http_req_failed: ${(failedRate * 100).toFixed(2)}% (임계값 ${ERROR_RATE_THRESHOLD * 100}%)\n` +
            `• 🕒 p95: ${p95.toFixed(2)} ms (임계값 ${P95_THRESHOLD_MS} ms)\n` +
            `• 🚨 초과 항목: ` +
            `${isErrorRateBad ? 'ErrorRate ' : ''}` +
            `${isP95Bad ? 'p95 ' : ''}`;

        const payload = JSON.stringify({content});

        http.post(webhookUrl, payload, {
            headers: {'Content-Type': 'application/json'},
        });
    }

    // === CI 콘솔 리포트 출력 ===
    return {
        stdout: textSummary(data, {indent: " ", enableColors: true}),
    };
}
