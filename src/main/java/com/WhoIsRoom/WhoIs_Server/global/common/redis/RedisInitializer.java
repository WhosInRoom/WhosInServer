package com.WhoIsRoom.WhoIs_Server.global.common.redis;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class RedisInitializer {

    private final RedisTemplate<String,String> redisTemplate;

    @PostConstruct
    public void clearRedis() {
        redisTemplate.getConnectionFactory()
                .getConnection()
                .flushDb(); // 선택한 DB(예: database: 1)만 초기화
        log.info("✅ Redis DB 초기화 완료");
    }
}

