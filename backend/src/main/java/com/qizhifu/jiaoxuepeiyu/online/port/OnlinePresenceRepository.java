package com.qizhifu.jiaoxuepeiyu.online.port;

import com.qizhifu.jiaoxuepeiyu.online.model.OnlineUser;
import com.qizhifu.jiaoxuepeiyu.online.model.OnlineUserQuery;
import java.time.LocalDateTime;
import java.util.List;

public interface OnlinePresenceRepository {

    void updateHeartbeat(Long userId, String ipAddress, LocalDateTime heartbeatAt);

    void markOffline(Long userId);

    List<OnlineUser> findUsers(OnlineUserQuery query, LocalDateTime onlineCutoff);

    long countUsers(OnlineUserQuery query);

    long countOnlineUsers(OnlineUserQuery query, LocalDateTime onlineCutoff);
}
