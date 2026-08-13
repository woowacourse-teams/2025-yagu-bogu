export interface Env {
    // Environment
    ENV: string;

    // JWT 액세스 토큰 파싱용 시크릿 (Github Actions를 통해 주입됨)
    JWT_ACCESS_TOKEN_SECRET_KEY: string;

    // 기상청 API 연동 키
    KMA_API_KEY: string;
}
