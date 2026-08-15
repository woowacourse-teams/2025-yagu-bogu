package com.yagubogu.place.dto;

/**
 * 구장 x 카테고리 조합 단위의 동기화 성공/실패 집계.
 * 조합 하나가 실패해도 나머지는 계속 진행되므로 둘을 함께 봐야 실제 상태를 알 수 있다.
 */
public record PlaceSyncResult(int success, int failed) {
}
