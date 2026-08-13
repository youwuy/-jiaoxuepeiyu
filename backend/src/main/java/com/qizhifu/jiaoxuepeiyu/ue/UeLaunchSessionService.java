package com.qizhifu.jiaoxuepeiyu.ue;

import com.qizhifu.jiaoxuepeiyu.auth.port.TokenGenerator;
import com.qizhifu.jiaoxuepeiyu.auth.security.TokenHash;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import com.qizhifu.jiaoxuepeiyu.common.validation.InputValidator;
import com.qizhifu.jiaoxuepeiyu.ue.model.UeLaunchSession;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UeLaunchSessionService {

    private static final Duration SESSION_VALIDITY = Duration.ofHours(8);

    private final TokenGenerator tokenGenerator;
    private final Map<String, SessionRecord> sessions = new ConcurrentHashMap<String, SessionRecord>();
    private Clock clock;

    @Autowired
    public UeLaunchSessionService(TokenGenerator tokenGenerator) {
        this(tokenGenerator, Clock.systemDefaultZone());
    }

    UeLaunchSessionService(TokenGenerator tokenGenerator, Clock clock) {
        this.tokenGenerator = tokenGenerator;
        this.clock = clock;
    }

    public UeLaunchSession create(Long studentId, Long trainingId, Long topicId, Long roomId) {
        requireId(studentId, "Student id is required");
        requireId(trainingId, "Training id is required");
        requireId(topicId, "Training topic id is required");
        removeExpiredSessions();

        String token = tokenGenerator.generate(studentId);
        Instant expiresAt = clock.instant().plus(SESSION_VALIDITY);
        sessions.put(TokenHash.sha256(token), new SessionRecord(studentId, trainingId, topicId, roomId, expiresAt));

        UeLaunchSession session = new UeLaunchSession();
        session.setLaunchToken(token);
        session.setStudentId(studentId);
        session.setTrainingId(trainingId);
        session.setTopicId(topicId);
        session.setRoomId(roomId);
        session.setExpiresAt(LocalDateTime.ofInstant(expiresAt, clock.getZone()));
        return session;
    }

    public Long requireStudentId(String token) {
        return requireRecord(token).studentId;
    }

    public Long requireStudentId(String token, Long trainingId) {
        return requireScope(token, trainingId).getStudentId();
    }

    public LaunchScope requireScope(String token, Long trainingId) {
        SessionRecord record = requireRecord(token);
        if (!record.trainingId.equals(trainingId)) {
            throw new BusinessException(401, "UE launch token does not match training");
        }
        return new LaunchScope(record.studentId, record.trainingId, record.topicId, record.roomId);
    }

    void setClock(Clock clock) {
        this.clock = clock;
    }

    private SessionRecord requireRecord(String token) {
        if (!InputValidator.hasText(token)) {
            throw new BusinessException(401, "Missing UE launch token");
        }
        String tokenHash = TokenHash.sha256(token.trim());
        SessionRecord record = sessions.get(tokenHash);
        if (record == null || !record.expiresAt.isAfter(clock.instant())) {
            sessions.remove(tokenHash);
            throw new BusinessException(401, "UE launch token is invalid or expired");
        }
        return record;
    }

    private void removeExpiredSessions() {
        Instant now = clock.instant();
        for (Map.Entry<String, SessionRecord> entry : sessions.entrySet()) {
            if (!entry.getValue().expiresAt.isAfter(now)) {
                sessions.remove(entry.getKey(), entry.getValue());
            }
        }
    }

    private void requireId(Long value, String message) {
        if (value == null || value.longValue() <= 0L) {
            throw new BusinessException(400, message);
        }
    }

    private static class SessionRecord {
        private final Long studentId;
        private final Long trainingId;
        private final Long topicId;
        private final Long roomId;
        private final Instant expiresAt;

        private SessionRecord(Long studentId, Long trainingId, Long topicId, Long roomId, Instant expiresAt) {
            this.studentId = studentId;
            this.trainingId = trainingId;
            this.topicId = topicId;
            this.roomId = roomId;
            this.expiresAt = expiresAt;
        }
    }

    public static class LaunchScope {
        private final Long studentId;
        private final Long trainingId;
        private final Long topicId;
        private final Long roomId;

        private LaunchScope(Long studentId, Long trainingId, Long topicId, Long roomId) {
            this.studentId = studentId;
            this.trainingId = trainingId;
            this.topicId = topicId;
            this.roomId = roomId;
        }

        public Long getStudentId() { return studentId; }
        public Long getTrainingId() { return trainingId; }
        public Long getTopicId() { return topicId; }
        public Long getRoomId() { return roomId; }
    }
}
