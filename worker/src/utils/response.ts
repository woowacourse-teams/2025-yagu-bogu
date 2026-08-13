// JSON 응답 생성 헬퍼
export function jsonResponse(
    data: any,
    status: number = 200,
    headers: Record<string, string> = {}
): Response {
    return new Response(
        JSON.stringify(data),
        {
            status,
            headers: {
                'Content-Type': 'application/json',
                ...headers
            }
        }
    );
}

// 에러 응답 생성 헬퍼
export function errorResponse(
    error: string,
    status: number = 500,
    hint?: string,
    headers: Record<string, string> = {}
): Response {
    const data: any = { error };
    if (hint) data.hint = hint;
    
    return jsonResponse(data, status, headers);
}
