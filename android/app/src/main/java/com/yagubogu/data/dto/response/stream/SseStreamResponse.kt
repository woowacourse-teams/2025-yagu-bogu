package com.yagubogu.data.dto.response.stream

import com.yagubogu.data.dto.response.checkin.FanRateByGameDto

/**
 * SSE(Server-Sent Event) 스트림에서 발생할 수 있는 모든 상황을 정의한 최상위 인터페이스입니다.
 */
sealed interface SseStreamResponse {
    /**
     * 연결 상태 관련
     * - ConnectionOpened: 연결 성공 (SSE 연결이 최초로 수립되었을 때 딱 한 번 발생)
     * - ConnectionClosed: 연결 종료 (SSE 연결이 정상적으로 끊어졌을 때 발생)
     * - Error: 에러 발생 (네트워크 끊김, 파싱 실패 등 예기치 못한 오류가 났을 때 발생)
     */
    data object ConnectionOpened : SseStreamResponse

    data object ConnectionClosed : SseStreamResponse

    data class Error(
        val error: Exception,
    ) : SseStreamResponse

    /**
     * 데이터 이벤트 관련 (서버가 보내주는 실제 메시지)
     * - CheckInCreated: 체크인 생성 이벤트
     * - Connect: 접속 확인 이벤트
     * - Timeout: 타임아웃 이벤트
     * - Comment: 핑/주석
     */
    data class CheckInCreated(
        val items: List<FanRateByGameDto>,
    ) : SseStreamResponse

    data class Connect(
        val data: String,
    ) : SseStreamResponse

    data class Timeout(
        val data: String,
    ) : SseStreamResponse

    data class Comment(
        val data: String,
    ) : SseStreamResponse
}
