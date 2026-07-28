package com.yagubogu.reward.client;

/**
 * 외부 주문 번호로 기프티콘 주문 상태를 조회한다.
 */
public interface GiftOrderStatusClient {

    /**
     * 외부 제공자의 응답을 내부에서 사용하는 조회 결과로 변환한다.
     */
    GiftOrderLookupResult findByExternalOrderId(String externalOrderId);
}
