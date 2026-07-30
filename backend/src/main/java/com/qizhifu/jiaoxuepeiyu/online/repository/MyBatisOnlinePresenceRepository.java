package com.qizhifu.jiaoxuepeiyu.online.repository;

import com.qizhifu.jiaoxuepeiyu.online.model.OnlineUser;
import com.qizhifu.jiaoxuepeiyu.online.model.OnlineUserQuery;
import com.qizhifu.jiaoxuepeiyu.online.port.OnlinePresenceRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisOnlinePresenceRepository implements OnlinePresenceRepository {

    private final OnlinePresenceMapper mapper;

    public MyBatisOnlinePresenceRepository(OnlinePresenceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void updateHeartbeat(Long userId, String ipAddress, LocalDateTime heartbeatAt) {
        mapper.updateHeartbeat(userId, ipAddress, heartbeatAt);
    }

    @Override
    public void markOffline(Long userId) {
        mapper.markOffline(userId);
    }

    @Override
    public List<OnlineUser> findUsers(OnlineUserQuery query, LocalDateTime onlineCutoff) {
        return mapper.findUsers(query, onlineCutoff);
    }

    @Override
    public long countUsers(OnlineUserQuery query) {
        return mapper.countUsers(query);
    }

    @Override
    public long countOnlineUsers(OnlineUserQuery query, LocalDateTime onlineCutoff) {
        return mapper.countOnlineUsers(query, onlineCutoff);
    }
}
