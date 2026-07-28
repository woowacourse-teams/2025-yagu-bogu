package com.yagubogu.reward.dto;

import java.time.LocalDateTime;

/**
 * 외부 조회에 필요한 기프티콘 대사 대상의 스냅샷이다.
 */
public record GifticonReconciliationTarget(
        long id,
        String externalOrderId,
        LocalDateTime requestStartedAt,
        int reconciliationAttemptCount
) {
}
