package com.qizhifu.jiaoxuepeiyu.ue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.qizhifu.jiaoxuepeiyu.auth.port.TokenGenerator;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import com.qizhifu.jiaoxuepeiyu.ue.model.UeLaunchSession;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class UeLaunchSessionServiceTests {

    private static final Instant NOW = Instant.parse("2026-08-05T10:00:00Z");

    @Test
    void createsScopedLaunchTokenAndResolvesStudent() {
        UeLaunchSessionService service = serviceAt(NOW);

        UeLaunchSession session = service.create(7L, 15L, 22L);

        assertEquals("launch-token", session.getLaunchToken());
        assertEquals(Long.valueOf(7L), service.requireStudentId("launch-token", 15L));
        assertEquals(Long.valueOf(7L), service.requireStudentId("launch-token"));
    }

    @Test
    void rejectsTokenForAnotherTraining() {
        UeLaunchSessionService service = serviceAt(NOW);
        service.create(7L, 15L, null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.requireStudentId("launch-token", 16L));

        assertEquals("UE launch token does not match training", exception.getMessage());
    }

    @Test
    void rejectsExpiredLaunchToken() {
        UeLaunchSessionService service = serviceAt(NOW);
        service.create(7L, 15L, null);
        service.setClock(Clock.fixed(NOW.plus(Duration.ofHours(9)), ZoneOffset.UTC));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.requireStudentId("launch-token", 15L));

        assertEquals("UE launch token is invalid or expired", exception.getMessage());
    }

    private UeLaunchSessionService serviceAt(Instant instant) {
        TokenGenerator generator = userId -> "launch-token";
        return new UeLaunchSessionService(generator, Clock.fixed(instant, ZoneOffset.UTC));
    }
}
