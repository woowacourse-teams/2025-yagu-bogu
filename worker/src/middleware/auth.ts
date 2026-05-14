import { jwtVerify } from 'jose';
import type { Env } from '../types';
import { Logger } from '../utils/logger';

// HMAC256 대칭키 기반 JWT 검증
export async function verifyHMACJWT(token: string, env: Env) {
    const logger = new Logger(env);
    try {
        const secretKey = env.JWT_ACCESS_TOKEN_SECRET_KEY;
        if (!secretKey) {
            throw new Error('JWT_ACCESS_TOKEN_SECRET_KEY is not configured.');
        }

        const secret = new TextEncoder().encode(secretKey);
        
        // Java 서버의 Algorithm.HMAC256(...) 검증 방식과 동일하게 
        // 발급된 JWT(대칭키)를 검증
        const { payload } = await jwtVerify(token, secret);
        return payload;
    } catch (error: any) {
        logger.error('❌ JWT 검증에 실패했습니다:', error.message || error);
        return null;
    }
}

// 인증 헤더 추출 및 검증 메인 함수
export async function authenticate(request: Request, env: Env) {
    const logger = new Logger(env);
    const authHeader = request.headers.get('Authorization');

    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        return {
            error: 'Authorization 헤더가 누락되었거나 올바르지 않습니다.',
            hint: '형식 예시: Authorization: Bearer YOUR_JWT_TOKEN',
            status: 401
        };
    }

    const jwt = authHeader.replace('Bearer ', '').trim();
    const payload = await verifyHMACJWT(jwt, env);

    if (!payload || !payload.sub) {
        logger.warn('⚠️ 유효하지 않거나 만료된 JWT 페이로드입니다:', { hasPayload: !!payload, hasSub: !!payload?.sub });
        return {
            error: '유효하지 않거나 만료된 JWT 토큰입니다.',
            hint: '다시 로그인하여 인증해 주세요.',
            status: 401
        };
    }

    return { userId: payload.sub as string };
}
