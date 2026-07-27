package com.yagubogu.reward.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GifticonIssuanceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 20, 12, 0);

    @DisplayName("당첨 발급 건은 수신자 정보 대기 상태로 생성된다")
    @Test
    void createAwaitingRecipientInfo() {
        GifticonIssuance issuance = new GifticonIssuance(null, null, "order-id", NOW);

        assertThat(issuance.getStatus()).isEqualTo(GifticonIssuanceStatus.AWAITING_RECIPIENT_INFO);
    }

    @DisplayName("전화번호를 등록하면서 발급 요청을 선점한다")
    @Test
    void prepareRequest() {
        GifticonIssuance issuance = new GifticonIssuance(null, null, "order-id", NOW);

        issuance.prepareRequest(new RecipientPhoneNumber("01012345678"), NOW.plusMinutes(1));

        assertThat(issuance.getStatus()).isEqualTo(GifticonIssuanceStatus.REQUESTING);
        assertThat(issuance.getRecipientPhoneNumber().getValue()).isEqualTo("01012345678");
    }

    @DisplayName("발송 요청을 선점한 뒤에는 다시 준비할 수 없다")
    @Test
    void rejectPreparingRequestAfterRequestStarted() {
        GifticonIssuance issuance = new GifticonIssuance(null, null, "order-id", NOW);
        issuance.prepareRequest(new RecipientPhoneNumber("01012345678"), NOW);

        assertThatThrownBy(() -> issuance.prepareRequest(
                new RecipientPhoneNumber("01087654321"), NOW.plusMinutes(1)))
                .isInstanceOf(InvalidGifticonIssuanceStateException.class);
    }
}
