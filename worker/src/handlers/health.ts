import { getCorsHeaders } from '../middleware/cors';
import { jsonResponse } from '../utils/response';

export function handleHealth(request: Request): Response {
    const corsHeaders = getCorsHeaders(request);
    
    return jsonResponse(
        { 
            status: 'ok',
            timestamp: new Date().toISOString()
        },
        200,
        corsHeaders
    );
}
