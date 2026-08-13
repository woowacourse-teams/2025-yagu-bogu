// 허용된 오리진 리스트
const ALLOWED_ORIGINS = [
    'https://yagubogu.com',
    'https://www.yagubogu.com',
];

// CORS 헤더를 동적으로 생성하는 함수
export function getCorsHeaders(request: Request): Record<string, string> {
    const origin = request.headers.get('Origin');
    const headers: Record<string, string> = {
        'Access-Control-Allow-Methods': 'GET, POST, PUT, OPTIONS',
        'Access-Control-Allow-Headers': 'Content-Type, Authorization',
    };

    if (origin && ALLOWED_ORIGINS.includes(origin)) {
        headers['Access-Control-Allow-Origin'] = origin;
    } else {
        // 허용되지 않은 오리진이거나 요청에 Origin 헤더가 없는 경우
        // 기본적으로 첫 번째 오리진을 주거나, 빈 값을 줄 수 있음
        headers['Access-Control-Allow-Origin'] = ALLOWED_ORIGINS[0];
    }

    return headers;
}

// CORS Preflight 처리
export function handleCorsOptions(request: Request): Response | null {
    if (request.method === 'OPTIONS') {
        return new Response(null, {
            headers: getCorsHeaders(request)
        });
    }
    return null;
}
