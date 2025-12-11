package com.project.login.service.flink.userbahavior;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSearchService {

    private final StringRedisTemplate redisTemplate;

    private static final String STREAM_KEY = "user_search_stream";

    public void recordSearch(Long userId, String keyword) {

        if (userId == null || keyword == null || keyword.isBlank()) {
            return;
        }

        Map<String, String> map = new HashMap<>();
        map.put("user_id", String.valueOf(userId));
        map.put("keyword", keyword);
        map.put("timestamp", String.valueOf(System.currentTimeMillis()));

        redisTemplate.opsForStream().add(STREAM_KEY, map);

        log.info("Recorded user search: userId={}, keyword={}", userId, keyword);
    }
}

