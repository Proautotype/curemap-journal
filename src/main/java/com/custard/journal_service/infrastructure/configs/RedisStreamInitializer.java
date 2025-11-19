package com.custard.journal_service.infrastructure.configs;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RedisStreamInitializer {

    private Logger logger = LoggerFactory.getLogger(RedisStreamInitializer.class);
    private final StringRedisTemplate redisTemplate;

    @PostConstruct
    public void init() {
        try {
            // If stream does not exist, create an empty one
            Boolean exists = redisTemplate.hasKey("journal_jobs");
            if (exists == null || !exists) {
                Map<String, String> map = new HashMap<>();
                map.put("init", "init");
                redisTemplate.opsForStream().add(
                        StreamRecords.string(map).withStreamKey("journal_jobs")
                );
            }

            // Try creating the group
            redisTemplate.opsForStream()
                    .createGroup("journal_jobs", ReadOffset.from("0-0"), "journal_group");

            logger.info("✔ Redis group created");
        } catch (Exception e) {
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }

            if (root instanceof io.lettuce.core.RedisBusyException ||
                    (root.getMessage() != null && root.getMessage().contains("BUSYGROUP"))) {
                logger.error("ℹ Consumer group already exists.");
            }

        }
    }
}

