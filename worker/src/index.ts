import type { Env } from './types';
import { handleCorsOptions, getCorsHeaders } from './middleware/cors';
import { handleHealth } from './handlers/health';
import { handleWeather } from './handlers/weather';
import { jsonResponse } from './utils/response';

export default {
    async fetch(request: Request, env: Env): Promise<Response> {
        // CORS Preflight 처리
        const corsResponse = handleCorsOptions(request);
        if (corsResponse) return corsResponse;

        const url = new URL(request.url);
        const corsHeaders = getCorsHeaders(request);

        // ========================================
        // 라우팅
        // ========================================

        // GET /health
        if (url.pathname === '/health') {
            return handleHealth(request);
        }

        // GET /api/stadium/weather
        if (url.pathname === '/api/stadium/weather' && request.method === 'GET') {
            return handleWeather(request, env);
        }

        // 404 Not Found
        return jsonResponse(
            {
                error: '경로를 찾을 수 없습니다',
                available_endpoints: ['/health', '/api/stadium/weather']
            },
            404,
            corsHeaders
        );
    }
};
