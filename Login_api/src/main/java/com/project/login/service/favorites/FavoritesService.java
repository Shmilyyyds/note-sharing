package com.project.login.service.favorites;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.project.login.convert.QuestionConvert;
import com.project.login.convert.SearchConvert;
import com.project.login.mapper.NoteStatsMapper;
import com.project.login.mapper.UserFavoriteNoteMapper;
import com.project.login.model.dataobject.NoteStatsDO;
import com.project.login.model.dataobject.QuestionDO;
import com.project.login.model.vo.NoteSearchVO;
import com.project.login.model.vo.qa.QuestionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoritesService {

    private final QuestionConvert questionConvert;
    private final MongoTemplate mongoTemplate;
    private final UserFavoriteNoteMapper userFavoriteNoteMapper;
    private final ElasticsearchClient esClient;
    
    @Qualifier("searchConvert")
    private final SearchConvert searchConvert;
    
    private final NoteStatsMapper noteStatsMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate redis;

    private static final String REDIS_KEY_PREFIX = "note_stats:";

    /**
     * 获取用户收藏的问题列表
     */
    public List<QuestionVO> getFavoriteQuestions(Long userId) {
        try {
            // 查询 MongoDB 中 favorites 列表包含该 userId 的所有问题
            Query query = new Query(Criteria.where("favorites").in(userId));
            List<QuestionDO> questions = mongoTemplate.find(query, QuestionDO.class);

            // 转换为 VO
            return questions.stream()
                    .map(questionConvert::toQuestionVO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取用户收藏问题列表失败, userId={}", userId, e);
            throw new RuntimeException("获取收藏问题列表失败", e);
        }
    }

    /**
     * 获取用户收藏的笔记列表
     */
    public List<NoteSearchVO> getFavoriteNotes(Long userId) {
        try {
            // 1. 从数据库查询用户收藏的笔记ID列表
            List<Long> noteIds = userFavoriteNoteMapper.selectNoteIdsByUserId(userId);
            if (noteIds.isEmpty()) {
                return Collections.emptyList();
            }

            // 2. 通过 Elasticsearch 查询这些笔记的详情
            List<NoteSearchVO> notes = new ArrayList<>();
            try {
                // 使用 ids 查询来批量获取笔记
                var response = esClient.search(s -> s
                                .index("notes")
                                .size(noteIds.size())
                                .query(q -> q
                                        .ids(ids -> ids
                                                .values(noteIds.stream()
                                                        .map(String::valueOf)
                                                        .collect(Collectors.toList()))
                                        )
                                ),
                        Object.class
                );

                response.hits().hits().forEach(hit -> {
                    NoteSearchVO vo = searchConvert.toSearchVO(hit.source());
                    if (vo != null) {
                        // 默认 contentSummary 为 title
                        if (vo.getContentSummary() == null) {
                            vo.setContentSummary(vo.getTitle());
                        }
                        notes.add(vo);
                    }
                });
            } catch (IOException e) {
                log.error("从 Elasticsearch 查询收藏笔记失败, userId={}", userId, e);
                throw new RuntimeException("查询收藏笔记失败", e);
            }

            if (notes.isEmpty()) {
                return Collections.emptyList();
            }

            // 3. 批量加载 Redis 统计数据
            Map<Long, NoteStatsDO> statsMap = loadStatsFromRedisThenMySQL(noteIds);

            // 4. 将统计数据写入 VO
            notes.forEach(vo -> {
                NoteStatsDO stats = statsMap.get(vo.getNoteId());
                if (stats != null) {
                    vo.setAuthorName(stats.getAuthorName());
                    vo.setViewCount(stats.getViews().intValue());
                    vo.setLikeCount(stats.getLikes().intValue());
                    vo.setFavoriteCount(stats.getFavorites().intValue());
                    vo.setCommentCount(stats.getComments().intValue());
                    vo.setUpdatedAt(stats.getUpdatedAt());
                }
            });

            // 5. 按收藏时间倒序排序（最新的在前）
            // 注意：这里我们按照 noteIds 的顺序来排序，因为 selectNoteIdsByUserId 已经按收藏时间倒序
            Map<Long, Integer> orderMap = new HashMap<>();
            for (int i = 0; i < noteIds.size(); i++) {
                orderMap.put(noteIds.get(i), i);
            }
            notes.sort((a, b) -> {
                Integer orderA = orderMap.getOrDefault(a.getNoteId(), Integer.MAX_VALUE);
                Integer orderB = orderMap.getOrDefault(b.getNoteId(), Integer.MAX_VALUE);
                return orderA.compareTo(orderB);
            });

            return notes;
        } catch (Exception e) {
            log.error("获取用户收藏笔记列表失败, userId={}", userId, e);
            throw new RuntimeException("获取收藏笔记列表失败", e);
        }
    }

    /**
     * 批量从 Redis 加载统计数据，如果 Redis 没有则从 MySQL 取并写入 Redis
     */
    private Map<Long, NoteStatsDO> loadStatsFromRedisThenMySQL(List<Long> ids) {
        Map<Long, NoteStatsDO> result = new HashMap<>();
        HashOperations<String, Object, Object> ops = redis.opsForHash();

        for (Long id : ids) {
            String key = REDIS_KEY_PREFIX + id;
            Map<Object, Object> map = ops.entries(key);

            if (map == null || map.isEmpty()) {
                // Redis 未命中 → 查询 MySQL
                NoteStatsDO db = noteStatsMapper.getById(id);
                if (db == null) {
                    db = defaultStats(id);
                }
                writeStatsToRedis(key, ops, db);
                map = ops.entries(key);
            }

            result.put(id, mapToStats(id, map));
        }

        return result;
    }

    private void writeStatsToRedis(String key, HashOperations<String, Object, Object> ops, NoteStatsDO db) {
        ops.put(key, "authorName", db.getAuthorName());
        ops.put(key, "views", String.valueOf(db.getViews()));
        ops.put(key, "likes", String.valueOf(db.getLikes()));
        ops.put(key, "favorites", String.valueOf(db.getFavorites()));
        ops.put(key, "comments", String.valueOf(db.getComments()));
        ops.put(key, "last_activity_at", db.getLastActivityAt() == null ? "" : db.getLastActivityAt().toString());
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
    }

    private NoteStatsDO mapToStats(Long id, Map<Object, Object> map) {
        NoteStatsDO stats = new NoteStatsDO();
        stats.setNoteId(id);
        stats.setAuthorName(map.get("authorName") != null ? map.get("authorName").toString() : "未知作者");
        stats.setViews(Long.parseLong(map.getOrDefault("views", "0").toString()));
        stats.setLikes(Long.parseLong(map.getOrDefault("likes", "0").toString()));
        stats.setFavorites(Long.parseLong(map.getOrDefault("favorites", "0").toString()));
        stats.setComments(Long.parseLong(map.getOrDefault("comments", "0").toString()));

        String updatedStr = map.getOrDefault("updatedAt", "").toString();
        if (updatedStr.isEmpty()) {
            String lastActivityAt = map.getOrDefault("last_activity_at", "").toString();
            stats.setUpdatedAt(lastActivityAt.isEmpty() ? null : LocalDateTime.parse(lastActivityAt));
        } else {
            stats.setUpdatedAt(LocalDateTime.parse(updatedStr));
        }

        stats.setVersion(Long.parseLong(map.getOrDefault("version", "0").toString()));
        return stats;
    }

    private NoteStatsDO defaultStats(Long noteId) {
        NoteStatsDO stats = new NoteStatsDO();
        stats.setNoteId(noteId);
        stats.setAuthorName("未知作者");
        stats.setViews(0L);
        stats.setLikes(0L);
        stats.setFavorites(0L);
        stats.setComments(0L);
        stats.setVersion(0L);
        stats.setUpdatedAt(null);
        return stats;
    }
}
