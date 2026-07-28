package com.yagubogu.reward.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 기프티콘 주문 대사의 운영 설정을 관리한다.
 */
@Validated
@ConfigurationProperties(prefix = "reward.gifticon.reconciliation")
public record GifticonReconciliationProperties(
        @NotNull(message = "Gifticon reconciliation initial delay must be configured")
        Duration initialDelay
) {

    @AssertTrue(message = "Gifticon reconciliation initial delay must be positive")
    public boolean isInitialDelayPositive() {
        return initialDelay == null || (!initialDelay.isZero() && !initialDelay.isNegative());
    }
}
