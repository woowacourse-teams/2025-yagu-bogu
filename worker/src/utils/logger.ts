import { Env } from '../types';

export class Logger {
    private env: Env;

    constructor(env: Env) {
        this.env = env;
    }

    private get isDevelopment(): boolean {
        // ENV가 없거나 'development'일 때 개발 환경으로 간주
        return !this.env.ENV || this.env.ENV === 'development';
    }

    log(...args: any[]) {
        if (this.isDevelopment) {
            console.log(...args);
        }
    }

    info(...args: any[]) {
        if (this.isDevelopment) {
            console.info(...args);
        }
    }

    warn(...args: any[]) {
        if (this.isDevelopment) {
            console.warn(...args);
        }
    }

    error(...args: any[]) {
        // 에러는 항상 출력
        console.error(...args);
    }
}
