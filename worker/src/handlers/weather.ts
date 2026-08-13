import type { Env } from '../types';
import { jsonResponse } from '../utils/response';
import { authenticate } from '../middleware/auth';
import { getCorsHeaders } from '../middleware/cors';
import { Logger } from '../utils/logger';

// ─────────────────────────────────────────────────────────────
// 전역 상수 (변경 없음)
// ─────────────────────────────────────────────────────────────
const CACHE_TTL_SECONDS = 3600;
const KMA_TIMEOUT_MS = 3500;
const KMA_NCST_NUM_OF_ROWS = 10;
const KMA_FCST_NUM_OF_ROWS = 60;
const CONCURRENT_LIMIT = 5;
const STRONG_WIND_MS = 10;

const CACHE_KEY_BASE = 'https://kma-grid-cache4.internal';
const KMA_BASE_URL = 'https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0';

// ─────────────────────────────────────────────────────────────
// 경기장 목록 (변경 없음)
// ─────────────────────────────────────────────────────────────
const ALL_STADIUMS = [
    { id: 1, name: '광주 기아 챔피언스필드', lat: 35.168139, lng: 126.889111, nx: 59, ny: 75 },
    { id: 2, name: '잠실 야구장', lat: 37.512150, lng: 127.071976, nx: 61, ny: 126 },
    { id: 3, name: '고척 스카이돔', lat: 37.498222, lng: 126.867250, nx: 58, ny: 125 },
    { id: 4, name: '수원 KT 위즈파크', lat: 37.299759, lng: 127.009781, nx: 60, ny: 121 },
    { id: 5, name: '대구 삼성 라이온즈파크', lat: 35.841111, lng: 128.681667, nx: 90, ny: 90 },
    { id: 6, name: '사직야구장', lat: 35.194077, lng: 129.061584, nx: 98, ny: 76 },
    { id: 7, name: '인천 SSG 랜더스필드', lat: 37.436778, lng: 126.693306, nx: 55, ny: 124 },
    { id: 8, name: '창원 NC 파크', lat: 35.222754, lng: 128.582251, nx: 89, ny: 76 },
    { id: 9, name: '대전 한화생명 볼파크', lat: 36.316589, lng: 127.431211, nx: 68, ny: 100 },
    { id: 10, name: '울산 문수 야구장', lat: 35.532334, lng: 129.265575, nx: 101, ny: 84 },
    { id: 11, name: '월명종합경기장 야구장', lat: 35.966360, lng: 126.748161, nx: 56, ny: 92 },
    { id: 12, name: '청주 야구장', lat: 36.638840, lng: 127.470149, nx: 69, ny: 107 },
    { id: 13, name: '포항 야구장', lat: 36.008273, lng: 129.359410, nx: 102, ny: 94 },
    { id: 14, name: '한화생명 이글스파크', lat: 36.317178, lng: 127.429167, nx: 68, ny: 100 },
    { id: 15, name: '대구시민운동장 야구장', lat: 35.881162, lng: 128.586371, nx: 89, ny: 91 },
    { id: 16, name: '무등 야구장', lat: 35.169165, lng: 126.887245, nx: 59, ny: 75 },
    { id: 17, name: '마산 야구장', lat: 35.220855, lng: 128.581050, nx: 89, ny: 76 },
    { id: 18, name: '숭의 야구장', lat: 37.466591, lng: 126.643239, nx: 54, ny: 124 },
    { id: 19, name: '삼성 라이온즈 볼파크', lat: 35.864844, lng: 128.805667, nx: 93, ny: 90 },
] as const;

type Stadium = typeof ALL_STADIUMS[number];
const STADIUM_MAP = new Map<number, Stadium>(ALL_STADIUMS.map(s => [s.id, s]));

// ─────────────────────────────────────────────────────────────
// KST 파트 추출 공통 유틸 (변경 없음)
// ─────────────────────────────────────────────────────────────
function getKstParts(date: Date) {
    const fmt = new Intl.DateTimeFormat('ko-KR', {
        timeZone: 'Asia/Seoul',
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit', hour12: false,
    });
    const m = new Map(fmt.formatToParts(date).map(p => [p.type, p.value]));
    return {
        year: m.get('year')!,
        month: m.get('month')!,
        day: m.get('day')!,
        hour: parseInt(m.get('hour') ?? '0', 10),
        min: parseInt(m.get('minute') ?? '0', 10),
    };
}

function getNcstBaseDateTime(): { baseDate: string; baseTime: string } {
    const now = new Date();
    let { year, month, day, hour, min } = getKstParts(now);
    if (min < 40) {
        hour -= 1;
        if (hour < 0) {
            const prev = getKstParts(new Date(now.getTime() - 86_400_000));
            return { baseDate: `${prev.year}${prev.month}${prev.day}`, baseTime: '2300' };
        }
    }
    return { baseDate: `${year}${month}${day}`, baseTime: `${String(hour).padStart(2, '0')}00` };
}

function getFcstBaseDateTime(): { baseDate: string; baseTime: string } {
    const now = new Date();
    let { year, month, day, hour, min } = getKstParts(now);
    if (min < 45) {
        hour -= 1;
        if (hour < 0) {
            const prev = getKstParts(new Date(now.getTime() - 86_400_000));
            return { baseDate: `${prev.year}${prev.month}${prev.day}`, baseTime: '2330' };
        }
    }
    return { baseDate: `${year}${month}${day}`, baseTime: `${String(hour).padStart(2, '0')}30` };
}

// ─────────────────────────────────────────────────────────────
// RN1 강수량 파싱 — ✅ 로그 추가
// ─────────────────────────────────────────────────────────────
function parseRainAmount(val: string, logger?: Logger): number {
    let result: number;
    let matchType: string;

    if (val === '강수없음' || val === '0') {
        result = 0; matchType = 'exact-zero';
    } else if (val.includes('미만')) {
        result = 0.5; matchType = 'less-than-1mm';
    } else {
        const rangeMatch = val.match(/(\d+(?:\.\d+)?)\s*~\s*(\d+(?:\.\d+)?)/);
        if (rangeMatch) {
            result = (parseFloat(rangeMatch[1]) + parseFloat(rangeMatch[2])) / 2;
            matchType = `range(${rangeMatch[1]}~${rangeMatch[2]})`;
        } else {
            const numMatch = val.match(/\d+(?:\.\d+)?/);
            result = numMatch ? parseFloat(numMatch[0]) : 0;
            matchType = numMatch ? `numeric(${numMatch[0]})` : 'fallback-zero';
        }
    }

    logger?.log(`🌧 [RN1 Parse] raw="${val}" → type=${matchType} → ${result}mm`);
    return result;
}

// ─────────────────────────────────────────────────────────────
// 중간 데이터 타입
// ─────────────────────────────────────────────────────────────
interface NcstData { t1h: number; pty: number; rn1: number; wsd: number; }
interface FcstData { sky: number; lgt: number; }
interface WeatherResult {
    condition: string; sky: string;
    temperature: string; precipitation: string; windSpeed: string;
}

// ─────────────────────────────────────────────────────────────
// 실황 items 파싱 — ✅ 로그 추가
// ─────────────────────────────────────────────────────────────
function parseNcst(items: any[], nx: number, ny: number, logger: Logger): NcstData {
    let t1h = 0, pty = 0, rn1 = 0, wsd = 0;
    for (const item of items) {
        const val: string = item.obsrValue;
        switch (item.category) {
            case 'T1H': t1h = parseFloat(val); break;
            case 'PTY': pty = parseInt(val, 10); break;
            case 'RN1': rn1 = parseRainAmount(val, logger); break;
            case 'WSD': wsd = parseFloat(val); break;
        }
    }
    logger.log(
        `🌡 [NCST Parsed] (${nx},${ny}) ` +
        `T1H=${t1h}°C PTY=${pty} RN1=${rn1}mm WSD=${wsd}m/s`
    );
    return { t1h, pty, rn1, wsd };
}

// ─────────────────────────────────────────────────────────────
// 예보 items → 최근접 슬롯 SKY·LGT 추출 — ✅ 로그 대폭 추가
// ─────────────────────────────────────────────────────────────
function parseFcst(items: any[], nx: number, ny: number, logger: Logger): FcstData {
    const nowMs = Date.now();

    const toUtcMs = (fcstDate: string, fcstTime: string): number => {
        const Y = +fcstDate.slice(0, 4);
        const M = +fcstDate.slice(4, 6) - 1;
        const D = +fcstDate.slice(6, 8);
        const H = +fcstTime.slice(0, 2);
        const m = +fcstTime.slice(2, 4);
        return Date.UTC(Y, M, D, H - 9, m);   // KST → UTC
    };

    // ── ① 전체 수신된 예보 슬롯 목록 로그 ──────────────────────
    const uniqueKeys = [...new Set(items.map(i => `${i.fcstDate}|${i.fcstTime}`))];
    const nowKstStr = new Date(nowMs + 9 * 3600_000).toISOString().replace('T', ' ').slice(0, 16);

    logger.log(
        `📋 [FCST Slots] (${nx},${ny}) ` +
        `nowKST=${nowKstStr} | ` +
        `slots=[${uniqueKeys.map(k => k.replace('|', ' ')).join(', ')}]`
    );

    // ── ② 각 슬롯별 시간 거리(분) 계산 후 선택 로그 ─────────────
    const slotWithDiff = uniqueKeys.map(key => {
        const [d, t] = key.split('|');
        const diffMin = Math.round((toUtcMs(d, t) - nowMs) / 60_000);
        return { key, diffMin };
    });

    slotWithDiff.forEach(({ key, diffMin }) => {
        const marker = Math.abs(diffMin) === Math.min(...slotWithDiff.map(s => Math.abs(s.diffMin)))
            ? ' ← 선택' : '';
        logger.log(`   slot ${key.replace('|', ' ')} | diff=${diffMin >= 0 ? '+' : ''}${diffMin}분${marker}`);
    });

    const nearestKey = uniqueKeys.reduce((best, key) => {
        const [d, t] = key.split('|');
        const [bd, bt] = best.split('|');
        return Math.abs(toUtcMs(d, t) - nowMs) < Math.abs(toUtcMs(bd, bt) - nowMs)
            ? key : best;
    }, uniqueKeys[0]);

    const [nearestDate, nearestTime] = nearestKey.split('|');
    const slot = items.filter(i => i.fcstDate === nearestDate && i.fcstTime === nearestTime);

    // ── ③ 선택된 슬롯의 raw 카테고리 값 전체 로그 ───────────────
    const slotRaw = slot.map(i => `${i.category}=${i.fcstValue}`).join(' ');
    logger.log(`🔍 [FCST Slot Raw] (${nx},${ny}) ${nearestDate} ${nearestTime} → ${slotRaw}`);

    let sky = 1, lgt = 0;
    for (const item of slot) {
        switch (item.category) {
            case 'SKY': sky = parseInt(item.fcstValue, 10); break;
            case 'LGT': lgt = parseFloat(item.fcstValue); break;
        }
    }

    // ── ④ SKY 해석 로그 ──────────────────────────────────────
    const skyLabel = sky === 1 ? '맑음(1)' : sky === 3 ? '구름많음(3)' : `흐림(${sky})`;
    logger.log(
        `☁️ [FCST Parsed] (${nx},${ny}) ` +
        `SKY=${skyLabel} LGT=${lgt}`
    );

    return { sky, lgt };
}

// ─────────────────────────────────────────────────────────────
// 실황 + 예보 병합 → WeatherResult — ✅ 로그 추가
// ─────────────────────────────────────────────────────────────
function mergeToWeather(
    ncst: NcstData,
    fcst: FcstData,
    nx: number,
    ny: number,
    logger: Logger,
): WeatherResult {
    const { t1h, pty, rn1, wsd } = ncst;
    const { sky, lgt } = fcst;

    let condition: string;
    let conditionReason: string;

    if (lgt > 0) {
        condition = 'THUNDERSTORM'; conditionReason = `LGT=${lgt}>0`;
    } else if (pty === 1 || pty === 5) {
        condition = rn1 >= 1.0 ? 'HEAVY_RAIN' : 'LIGHT_RAIN';
        conditionReason = `PTY=${pty} RN1=${rn1}(${rn1 >= 1.0 ? '≥1mm' : '<1mm'})`;
    } else if (pty === 2 || pty === 6) {
        condition = 'RAIN_SNOW'; conditionReason = `PTY=${pty}`;
    } else if (pty === 3 || pty === 7) {
        condition = 'SNOW'; conditionReason = `PTY=${pty}`;
    } else if (wsd >= STRONG_WIND_MS) {
        condition = 'STRONG_WIND'; conditionReason = `WSD=${wsd}≥${STRONG_WIND_MS}`;
    } else {
        condition = sky === 1 ? 'CLEAR' : sky === 3 ? 'PARTLY_CLOUDY' : 'CLOUDY';
        conditionReason = `SKY=${sky}`;
    }

    logger.log(
        `✅ [Merge Result] (${nx},${ny}) ` +
        `condition=${condition} (reason: ${conditionReason}) ` +
        `temp=${t1h}°C precip=${rn1}mm wind=${wsd}m/s sky=${sky}`
    );

    return {
        condition,
        sky: sky === 1 ? 'CLEAR' : sky === 3 ? 'PARTLY_CLOUDY' : 'CLOUDY',
        temperature: `${t1h}°C`,
        precipitation: `${rn1}mm`,
        windSpeed: `${wsd}m/s`,
    };
}

// ─────────────────────────────────────────────────────────────
// KMA API 공통 호출 유틸 (변경 없음)
// ─────────────────────────────────────────────────────────────
async function fetchKmaItems(url: string): Promise<any[]> {
    const controller = new AbortController();
    const tid = setTimeout(() => controller.abort(), KMA_TIMEOUT_MS);
    try {
        const res = await fetch(url, { signal: controller.signal });
        clearTimeout(tid);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const json: any = await res.json();
        const header = json?.response?.header;
        if (header?.resultCode !== '00') {
            throw new Error(`KMA: ${header?.resultMsg} (${header?.resultCode})`);
        }
        const items = json?.response?.body?.items?.item;
        if (!Array.isArray(items) || items.length === 0) throw new Error('KMA 응답 데이터 없음');
        return items;
    } catch (err) {
        clearTimeout(tid);
        throw err;
    }
}

// ─────────────────────────────────────────────────────────────
// 단일 격자 날씨 조회 — ✅ logger를 파서로 전달
// ─────────────────────────────────────────────────────────────
async function fetchGridWeather(
    nx: number,
    ny: number,
    serviceKey: string,
    ncstBase: { baseDate: string; baseTime: string },
    fcstBase: { baseDate: string; baseTime: string },
    cache: Cache,
    logger: Logger,
): Promise<WeatherResult> {
    const cacheKey = new Request(
        `${CACHE_KEY_BASE}/ncst-${ncstBase.baseDate}-${ncstBase.baseTime}/fcst-${fcstBase.baseTime}/${nx}/${ny}`,
        { method: 'GET' },
    );

    const cached = await cache.match(cacheKey);
    if (cached) {
        logger.log(`🚀 [Cache Hit] nx=${nx} ny=${ny}`);
        return cached.json<WeatherResult>();
    }

    const ncstUrl =
        `${KMA_BASE_URL}/getUltraSrtNcst` +
        `?serviceKey=${serviceKey}&pageNo=1&numOfRows=${KMA_NCST_NUM_OF_ROWS}&dataType=JSON` +
        `&base_date=${ncstBase.baseDate}&base_time=${ncstBase.baseTime}&nx=${nx}&ny=${ny}`;

    const fcstUrl =
        `${KMA_BASE_URL}/getUltraSrtFcst` +
        `?serviceKey=${serviceKey}&pageNo=1&numOfRows=${KMA_FCST_NUM_OF_ROWS}&dataType=JSON` +
        `&base_date=${fcstBase.baseDate}&base_time=${fcstBase.baseTime}&nx=${nx}&ny=${ny}`;

    try {
        const [ncstItems, fcstItems] = await Promise.all([
            fetchKmaItems(ncstUrl),
            fetchKmaItems(fcstUrl),
        ]);

        logger.log(`📦 [Raw Count] (${nx},${ny}) ncst=${ncstItems.length}행 fcst=${fcstItems.length}행`);

        const ncst = parseNcst(ncstItems, nx, ny, logger);
        const fcst = parseFcst(fcstItems, nx, ny, logger);
        const weather = mergeToWeather(ncst, fcst, nx, ny, logger);

        await cache.put(cacheKey, new Response(JSON.stringify(weather), {
            headers: {
                'Content-Type': 'application/json',
                'Cache-Control': `s-maxage=${CACHE_TTL_SECONDS}`,
            },
        }));
        logger.log(`💾 [Cache Stored] nx=${nx} ny=${ny}`);

        return weather;

    } catch (err: any) {
        logger.warn(`⚠️ 격자(${nx},${ny}) 조회 실패: ${err.message}`);
        return { condition: 'UNKNOWN', sky: 'N/A', temperature: 'N/A', precipitation: 'N/A', windSpeed: 'N/A' };
    }
}

// batchedAll, handleWeather 이하 변경 없음 (생략)
// ─────────────────────────────────────────────────────────────
// 동시 요청 수 제한 유틸
// ─────────────────────────────────────────────────────────────
async function batchedAll<T>(
    tasks: Array<() => Promise<T>>,
    concurrency: number,
): Promise<T[]> {
    const results: T[] = [];
    for (let i = 0; i < tasks.length; i += concurrency) {
        const batch = tasks.slice(i, i + concurrency).map(fn => fn());
        results.push(...await Promise.all(batch));
    }
    return results;
}

// ─────────────────────────────────────────────────────────────
// 메인 핸들러
// GET /api/stadium/weather?ids=1,2,5
// ─────────────────────────────────────────────────────────────
export async function handleWeather(request: Request, env: Env): Promise<Response> {
    const logger = new Logger(env);
    const corsHeaders = getCorsHeaders(request);

    const authResult = await authenticate(request, env);
    if ('error' in authResult) {
        return jsonResponse(
            { error: authResult.error, hint: authResult.hint },
            authResult.status ?? 401,
            corsHeaders,
        );
    }

    const url = new URL(request.url);
    const idsParam = url.searchParams.get('ids');
    let targetStadiums: Stadium[];

    if (idsParam) {
        const ids = idsParam
            .split(',')
            .map(s => parseInt(s.trim(), 10))
            .filter(n => !isNaN(n));

        if (ids.length === 0) {
            return jsonResponse({ error: '유효한 경기장 ID가 없습니다.' }, 400, corsHeaders);
        }

        targetStadiums = ids.reduce<Stadium[]>((acc, id) => {
            const s = STADIUM_MAP.get(id);
            if (s) acc.push(s);
            return acc;
        }, []);

        if (targetStadiums.length === 0) {
            return jsonResponse({ error: '일치하는 경기장이 없습니다.' }, 400, corsHeaders);
        }
    } else {
        targetStadiums = [...ALL_STADIUMS];
    }

    try {
        if (!env.KMA_API_KEY) throw new Error('KMA_API_KEY missing');

        const serviceKey = encodeURIComponent(env.KMA_API_KEY);
        const ncstBase = getNcstBaseDateTime();   // 실황 base time
        const fcstBase = getFcstBaseDateTime();   // 예보 base time
        const cache = caches.default;

        const uniqueGrids = [...new Map(
            targetStadiums.map(s => [`${s.nx}:${s.ny}`, { nx: s.nx, ny: s.ny }]),
        ).values()];

        const gridTasks = uniqueGrids.map(
            ({ nx, ny }) => () =>
                fetchGridWeather(nx, ny, serviceKey, ncstBase, fcstBase, cache, logger)
                    .then(weather => ({ key: `${nx}:${ny}`, weather })),
        );
        const gridResults = await batchedAll(gridTasks, CONCURRENT_LIMIT);

        const gridWeatherMap = new Map(gridResults.map(r => [r.key, r.weather]));

        const data = targetStadiums.map(s => ({
            id: s.id,
            name: s.name,
            lat: s.lat,
            lng: s.lng,
            nx: s.nx,
            ny: s.ny,
            weather: gridWeatherMap.get(`${s.nx}:${s.ny}`)
                ?? { condition: 'UNKNOWN', sky: 'N/A', temperature: 'N/A', precipitation: 'N/A', windSpeed: 'N/A' },
        }));

        return jsonResponse(
            {
                success: true,
                count: data.length,
                ncstBaseDate: ncstBase.baseDate,
                ncstBaseTime: ncstBase.baseTime,
                fcstBaseDate: fcstBase.baseDate,
                fcstBaseTime: fcstBase.baseTime,
                data,
            },
            200,
            corsHeaders,
        );

    } catch (error: any) {
        logger.error('❌ 서버 내부 오류:', error.message);
        return jsonResponse(
            { error: '날씨 데이터를 가져오지 못했습니다.', message: error.message },
            500,
            corsHeaders,
        );
    }
}