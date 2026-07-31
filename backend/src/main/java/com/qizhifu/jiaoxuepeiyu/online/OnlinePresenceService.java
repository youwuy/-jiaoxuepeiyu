package com.qizhifu.jiaoxuepeiyu.online;

import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import com.qizhifu.jiaoxuepeiyu.common.validation.InputValidator;
import com.qizhifu.jiaoxuepeiyu.online.model.OnlineHeartbeatResult;
import com.qizhifu.jiaoxuepeiyu.online.model.OnlinePresenceDashboard;
import com.qizhifu.jiaoxuepeiyu.online.model.OnlineUser;
import com.qizhifu.jiaoxuepeiyu.online.model.OnlineUserQuery;
import com.qizhifu.jiaoxuepeiyu.online.port.OnlinePresenceRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OnlinePresenceService {

    private static final int DEFAULT_LIMIT = 100;
    private static final int MAX_LIMIT = 500;

    private final OnlinePresenceRepository repository;
    private final Clock clock;
    private final int heartbeatIntervalSeconds;
    private final int offlineTimeoutSeconds;

    @Autowired
    public OnlinePresenceService(OnlinePresenceRepository repository,
                                 @Value("${app.online.heartbeat-interval-seconds:30}") int heartbeatIntervalSeconds,
                                 @Value("${app.online.offline-timeout-seconds:120}") int offlineTimeoutSeconds) {
        this(repository, Clock.systemDefaultZone(), heartbeatIntervalSeconds, offlineTimeoutSeconds);
    }

    OnlinePresenceService(OnlinePresenceRepository repository,
                          Clock clock,
                          int heartbeatIntervalSeconds,
                          int offlineTimeoutSeconds) {
        this.repository = repository;
        this.clock = clock;
        this.heartbeatIntervalSeconds = heartbeatIntervalSeconds;
        this.offlineTimeoutSeconds = offlineTimeoutSeconds;
    }

    public OnlineHeartbeatResult heartbeat(Long userId, String ipAddress) {
        requireUserId(userId);
        LocalDateTime heartbeatAt = now();
        repository.updateHeartbeat(userId, trimToNull(ipAddress), heartbeatAt);

        OnlineHeartbeatResult result = new OnlineHeartbeatResult();
        result.setHeartbeatAt(heartbeatAt);
        result.setHeartbeatIntervalSeconds(Integer.valueOf(heartbeatIntervalSeconds));
        result.setOfflineTimeoutSeconds(Integer.valueOf(offlineTimeoutSeconds));
        return result;
    }

    public void markOffline(Long userId) {
        requireUserId(userId);
        repository.markOffline(userId);
    }

    public OnlinePresenceDashboard listOnlineUsers(OnlineUserQuery query) {
        OnlineUserQuery normalized = normalizeQuery(query);
        LocalDateTime cutoff = now().minusSeconds(offlineTimeoutSeconds);
        List<OnlineUser> users = repository.findUsers(normalized, cutoff);
        long totalCount = repository.countUsers(normalized);
        long onlineCount = repository.countOnlineUsers(normalized, cutoff);

        OnlinePresenceDashboard dashboard = new OnlinePresenceDashboard();
        dashboard.setGeneratedAt(now());
        dashboard.setTotalCount(Long.valueOf(totalCount));
        dashboard.setOnlineCount(Long.valueOf(onlineCount));
        dashboard.setOfflineCount(Long.valueOf(Math.max(0L, totalCount - onlineCount)));
        dashboard.setHeartbeatIntervalSeconds(Integer.valueOf(heartbeatIntervalSeconds));
        dashboard.setOfflineTimeoutSeconds(Integer.valueOf(offlineTimeoutSeconds));
        dashboard.setUsers(users);
        return dashboard;
    }

    private OnlineUserQuery normalizeQuery(OnlineUserQuery source) {
        OnlineUserQuery query = new OnlineUserQuery();
        if (source != null) {
            query.setUserType(normalizeUserType(source.getUserType()));
            query.setKeyword(like(trimToNull(source.getKeyword())));
            query.setOnlineOnly(source.getOnlineOnly());
            query.setLimit(limit(source.getLimit()));
        } else {
            query.setLimit(Integer.valueOf(DEFAULT_LIMIT));
        }
        if (query.getLimit() == null) {
            query.setLimit(Integer.valueOf(DEFAULT_LIMIT));
        }
        return query;
    }

    private String normalizeUserType(String userType) {
        String normalized = trimToNull(userType);
        return normalized == null ? null : normalized.toLowerCase(Locale.ENGLISH);
    }

    private Integer limit(Integer value) {
        if (value == null) {
            return Integer.valueOf(DEFAULT_LIMIT);
        }
        if (value.intValue() <= 0) {
            throw new BusinessException(400, "Limit is invalid");
        }
        return Integer.valueOf(Math.min(value.intValue(), MAX_LIMIT));
    }

    private String like(String value) {
        return value == null ? null : "%" + value + "%";
    }

    private String trimToNull(String value) {
        return InputValidator.hasText(value) ? value.trim() : null;
    }

    private void requireUserId(Long userId) {
        if (userId == null || userId.longValue() <= 0L) {
            throw new BusinessException(401, "Missing authenticated identity");
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
