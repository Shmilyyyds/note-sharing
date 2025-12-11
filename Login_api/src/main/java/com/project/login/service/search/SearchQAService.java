package com.project.login.service.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.login.convert.QuestionConvert;
import com.project.login.model.dataobject.QuestionDO;
import com.project.login.model.vo.qa.QuestionVO;
import com.project.login.repository.QuestionRepository;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchQAService {

    private final ElasticsearchClient esClient;
    private final QuestionRepository questionRepository; // MongoDB
    private final RedisTemplate<String, Object> redisTemplate;
    private final QuestionConvert convert;
    private final ObjectMapper objectMapper;
    @Resource
    private StringRedisTemplate redis;

    private static final String REDIS_KEY_PREFIX = "question:detail:";

    public List<QuestionVO> searchQuestions(String keyword) {

        List<ScoredQuestion> scoredList = new ArrayList<>();

        // ES 搜索，只获取 questionId
        try {
            var response = esClient.search(s -> s
                            .index("questions")
                            .size(30)
                            .query(q -> q
                                    .multiMatch(m -> m
                                            .query(keyword)
                                            .fields("title^3", "content", "tags^2")
                                    )
                            ),
                    Object.class
            );

            response.hits().hits().forEach(hit -> {
                if (hit.source() == null) return;

                // 获取 questionId
                Map<String, Object> src = (Map<String, Object>) hit.source();
                String questionId = (String) src.get("questionId");

                double score = hit.score() != null ? hit.score() : 0;
                scoredList.add(new ScoredQuestion(questionId, score));
            });

        } catch (IOException e) {
            throw new RuntimeException("ES 搜索失败", e);
        }

        if (scoredList.isEmpty()) return Collections.emptyList();

        // 批量加载问答数据（Redis → MongoDB）
        List<String> qids = scoredList.stream()
                .map(s -> s.questionId)
                .toList();

        Map<String, QuestionDO> detailMap = loadQuestionDetailBatch(qids);

        // 转换成 VO，并计算最新活跃时间
        scoredList.forEach(s -> {
            QuestionDO data = detailMap.get(s.questionId);
            if (data != null) {
                s.vo = convert.toQuestionVO(data);
                s.updatedAt = getLatestActivity(data); // 赋值最新活跃时间
            }
        });

        // 综合排序：ES score + 点赞/收藏/回答数
        scoredList.sort((a, b) -> {
            double sa = a.score * 4 +
                    a.vo.getLikeCount() * 2 +
                    a.vo.getFavoriteCount() * 3+
                    a.vo.getAnswers().size() +
                    recencyScore(a.updatedAt);;

            double sb = b.score * 4 +
                    b.vo.getLikeCount() * 2 +
                    b.vo.getFavoriteCount() * 3 +
                    b.vo.getAnswers().size() +
                    recencyScore(b.updatedAt);;

            return Double.compare(sb, sa);
        });

        // 返回 QuestionVO 列表
        return scoredList.stream()
                .map(s -> s.vo)
                .collect(Collectors.toList());
    }

    // 批量加载 Redis → MongoDB
    private Map<String, QuestionDO> loadQuestionDetailBatch(List<String> ids) {
        Map<String, QuestionDO> result = new HashMap<>();

        for (String qid : ids) {
            String key = REDIS_KEY_PREFIX + qid;

            try {
                // 1. 先从 Redis 获取
                String json = redis.opsForValue().get(key);
                if (json != null) {
                    // Redis 命中，解析 JSON
                    QuestionDO q = objectMapper.readValue(json, QuestionDO.class);
                    result.put(qid, q);
                    continue;
                }

                // 2. Redis 未命中 → MongoDB 查询
                QuestionDO db = questionRepository.findById(qid).orElse(null);
                if (db != null) {
                    // 写入 Redis
                    String dbJson = objectMapper.writeValueAsString(db);
                    redis.opsForValue().set(key, dbJson, Duration.ofHours(2));
                    result.put(qid, db);
                }

            } catch (Exception e) {
                log.error("加载问题详情失败 qid={}", qid, e);
            }
        }

        return result;
    }

    // 提问、回答、评论、回复中最晚的时间
    private LocalDateTime getLatestActivity(QuestionDO q) {
        if (q == null) return null;

        LocalDateTime latest = q.getCreatedAt();

        for (var ans : q.getAnswers()) {
            latest = max(latest, ans.getCreatedAt());

            for (var c : ans.getComments()) {
                latest = max(latest, c.getCreatedAt());

                for (var r : c.getReplies()) {
                    latest = max(latest, r.getCreatedAt());
                }
            }
        }
        return latest;
    }

    private LocalDateTime max(LocalDateTime a, LocalDateTime b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.isAfter(b) ? a : b;
    }

    private double recencyScore(LocalDateTime time) {
        if (time == null) return 0;

        long millis = Duration.between(time, LocalDateTime.now()).toMillis();
        double days = millis / 86400000.0;
        return 1 / (days + 1);
    }

    private static class ScoredQuestion {
        String questionId;
        double score;
        QuestionVO vo;
        LocalDateTime updatedAt;

        ScoredQuestion(String qid, double score) {
            this.questionId = qid;
            this.score = score;
        }
    }
}


