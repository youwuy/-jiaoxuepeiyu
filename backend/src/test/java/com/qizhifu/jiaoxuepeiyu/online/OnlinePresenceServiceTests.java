package com.qizhifu.jiaoxuepeiyu.online;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.qizhifu.jiaoxuepeiyu.online.model.OnlineHeartbeatResult;
import com.qizhifu.jiaoxuepeiyu.online.model.OnlinePresenceDashboard;
import com.qizhifu.jiaoxuepeiyu.online.model.OnlineUser;
import com.qizhifu.jiaoxuepeiyu.online.model.OnlineUserQuery;
import com.qizhifu.jiaoxuepeiyu.online.port.OnlinePresenceRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class OnlinePresenceServiceTests {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-30T10:00:00Z"), ZoneId.of("Asia/Shanghai"));

    @Test
    void heartbeatUpdatesCurrentUserIpAndReturnsTimingMetadata() {
        FakeOnlinePresenceRepository repository = new FakeOnlinePresenceRepository();
        OnlinePresenceService service = new OnlinePresenceService(repository, CLOCK, 30, 120);

        OnlineHeartbeatResult result = service.heartbeat(7L, "192.168.1.8");

        assertEquals(Long.valueOf(7L), repository.heartbeatUserId);
        assertEquals("192.168.1.8", repository.heartbeatIpAddress);
        assertEquals(LocalDateTime.of(2026, 7, 30, 18, 0), repository.heartbeatAt);
        assertEquals(Integer.valueOf(30), result.getHeartbeatIntervalSeconds());
        assertEquals(Integer.valueOf(120), result.getOfflineTimeoutSeconds());
        assertEquals(LocalDateTime.of(2026, 7, 30, 18, 0), result.getHeartbeatAt());
    }

    @Test
    void listOnlineUsersUsesConfiguredOfflineCutoff() {
        FakeOnlinePresenceRepository repository = new FakeOnlinePresenceRepository();
        repository.users.add(user(1L, "teacher", LocalDateTime.of(2026, 7, 30, 17, 59)));
        repository.users.add(user(2L, "student", LocalDateTime.of(2026, 7, 30, 17, 55)));
        OnlinePresenceService service = new OnlinePresenceService(repository, CLOCK, 30, 120);
        OnlineUserQuery query = new OnlineUserQuery();
        query.setOnlineOnly(Boolean.TRUE);

        OnlinePresenceDashboard dashboard = service.listOnlineUsers(query);

        assertEquals(LocalDateTime.of(2026, 7, 30, 17, 58), repository.onlineCutoff);
        assertEquals(2L, dashboard.getTotalCount().longValue());
        assertEquals(1L, dashboard.getOnlineCount().longValue());
        assertEquals(1L, dashboard.getOfflineCount().longValue());
        assertEquals(1, dashboard.getUsers().size());
        assertEquals(Boolean.TRUE, dashboard.getUsers().get(0).getOnline());
        assertNotNull(dashboard.getGeneratedAt());
    }

    @Test
    void markOfflineClearsHeartbeatForUser() {
        FakeOnlinePresenceRepository repository = new FakeOnlinePresenceRepository();
        OnlinePresenceService service = new OnlinePresenceService(repository, CLOCK, 30, 120);

        service.markOffline(7L);

        assertEquals(Long.valueOf(7L), repository.offlineUserId);
    }

    private OnlineUser user(Long userId, String userType, LocalDateTime lastHeartbeatTime) {
        OnlineUser user = new OnlineUser();
        user.setUserId(userId);
        user.setUsername("user" + userId);
        user.setRealName("User " + userId);
        user.setUserType(userType);
        user.setLastLoginIp("127.0.0." + userId);
        user.setLastHeartbeatTime(lastHeartbeatTime);
        return user;
    }

    private static class FakeOnlinePresenceRepository implements OnlinePresenceRepository {
        private Long heartbeatUserId;
        private String heartbeatIpAddress;
        private LocalDateTime heartbeatAt;
        private Long offlineUserId;
        private LocalDateTime onlineCutoff;
        private final List<OnlineUser> users = new ArrayList<OnlineUser>();

        @Override
        public void updateHeartbeat(Long userId, String ipAddress, LocalDateTime heartbeatAt) {
            this.heartbeatUserId = userId;
            this.heartbeatIpAddress = ipAddress;
            this.heartbeatAt = heartbeatAt;
        }

        @Override
        public void markOffline(Long userId) {
            this.offlineUserId = userId;
        }

        @Override
        public List<OnlineUser> findUsers(OnlineUserQuery query, LocalDateTime onlineCutoff) {
            this.onlineCutoff = onlineCutoff;
            List<OnlineUser> result = new ArrayList<OnlineUser>();
            for (OnlineUser user : users) {
                boolean online = user.getLastHeartbeatTime() != null && !user.getLastHeartbeatTime().isBefore(onlineCutoff);
                user.setOnline(Boolean.valueOf(online));
                if (Boolean.TRUE.equals(query.getOnlineOnly()) && !online) {
                    continue;
                }
                result.add(user);
            }
            return result;
        }

        @Override
        public long countUsers(OnlineUserQuery query) {
            return users.size();
        }

        @Override
        public long countOnlineUsers(OnlineUserQuery query, LocalDateTime onlineCutoff) {
            long count = 0;
            for (OnlineUser user : users) {
                if (user.getLastHeartbeatTime() != null && !user.getLastHeartbeatTime().isBefore(onlineCutoff)) {
                    count++;
                }
            }
            return count;
        }
    }
}
