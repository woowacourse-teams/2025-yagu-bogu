package com.yagubogu.sse.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.yagubogu.sse.repository.SseEmitterRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class SseEmitterServiceTest {

    private final SseEmitterRepository repository = new SseEmitterRepository();
    private final SseEmitterService sservice = new SseEmitterService(repository);

    @DisplayName("emitter가 추가될 때 connect 이벤트를 보내고, Repository에 저장된다")
    @Test
    void add_shouldRegisterEmitterAndSendConnectEvent() {
        // when
        SseEmitter emitter = sservice.add();

        // then
        assertThat(repository.all()).contains(emitter);
    }
}
