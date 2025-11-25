package com.project.login.service.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.project.login.convert.SearchConvert;
import com.project.login.mapper.NoteStatsMapper;
import com.project.login.model.dataobject.NoteStatsDO;
import com.project.login.model.dto.search.NoteSearchDTO;
import com.project.login.model.vo.NoteSearchVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchClient esClient;

    @Qualifier("searchConvert")
    private final SearchConvert convert;

    private final NoteStatsMapper noteStatsMapper;

    private final StringRedisTemplate redis;

    private static final String REDIS_KEY_PREFIX = "note:stats:";

    public List<NoteSearchVO> searchNotes(NoteSearchDTO dto) {
        String keyword = dto.getKeyword();
        List<ScoredNote> scoredNotes = new ArrayList<>();

        try {
            var response = esClient.search(s -> s
                            .index("notes")
                            .size(30)
                            .query(q -> q
                                    .multiMatch(m -> m
                                            .query(keyword)
                                            .fields("title^2", "contentSummary")
                                    )
                            ),
                    Object.class
            );

            response.hits().hits().forEach(hit -> {
                NoteSearchVO vo = convert.toSearchVO(hit.source());
                double score = hit.score() != null ? hit.score() : 0.0;
                scoredNotes.add(new ScoredNote(vo, score));
            });

        } catch (IOException e) {
            throw new RuntimeException("搜索失败", e);
        }

        if (scoredNotes.isEmpty()) return Collections.emptyList();

        // ==========================
        // 批量加载 Redis 统计数据
        // ==========================
        List<Long> noteIds = scoredNotes.stream()
                .map(n -> n.vo.getNoteId())
                .collect(Collectors.toList());

        Map<Long, NoteStatsDO> statsMap = loadStatsFromRedisThenMySQL(noteIds);

        // 将统计数据写入 VO
        scoredNotes.forEach(sn -> {
            NoteStatsDO stats = statsMap.get(sn.vo.getNoteId());
            sn.vo.setViewCount(stats.getViews().intValue());
            sn.vo.setLikeCount(stats.getLikes().intValue());
            sn.vo.setFavoriteCount(stats.getFavorites().intValue());
            sn.vo.setCommentCount(stats.getComments().intValue());
            sn.updatedAt = stats.getUpdatedAt();
        });

        // ==========================
        // 综合排序
        // ==========================
        scoredNotes.sort((a, b) -> {
            double scoreA = a.score
                    + a.vo.getViewCount() + a.vo.getLikeCount()
                    + a.vo.getFavoriteCount() + a.vo.getCommentCount()
                    + recencyScore(a.updatedAt);
            double scoreB = b.score
                    + b.vo.getViewCount() + b.vo.getLikeCount()
                    + b.vo.getFavoriteCount() + b.vo.getCommentCount()
                    + recencyScore(b.updatedAt);
            return Double.compare(scoreB, scoreA);
        });

        return scoredNotes.stream().map(sn -> sn.vo).collect(Collectors.toList());
    }

    /**
     * 批量从 Redis 加载统计数据，如果 Redis 没有则从 MySQL 取并写入 Redis
     */
    private Map<Long, NoteStatsDO> loadStatsFromRedisThenMySQL(List<Long> ids) {
        Map<Long, NoteStatsDO> result = new HashMap<>();

        List<Long> missIds = new ArrayList<>();

        for (Long id : ids) {
            String redisKey = REDIS_KEY_PREFIX + id;
            Map<Object, Object> map = redis.opsForHash().entries(redisKey);

            if (map != null && !map.isEmpty()) {
                result.put(id, mapToStats(id, map));
            } else {
                missIds.add(id);
            }
        }

        // Redis 未命中 → 批量查询 MySQL
        if (!missIds.isEmpty()) {
            List<NoteStatsDO> fromDb = noteStatsMapper.getByIds(missIds);

            for (Long id : missIds) {
                NoteStatsDO stats = fromDb.stream()
                        .filter(s -> s.getNoteId().equals(id))
                        .findFirst()
                        .orElse(defaultStats(id));

                result.put(id, stats);
                writeStatsToRedis(stats); // 写入 Redis，提高后续命中率
            }
        }

        return result;
    }

    /**
     * 将 MySQL 数据写入 Redis 缓存
     */
    private void writeStatsToRedis(NoteStatsDO stats) {
        String key = REDIS_KEY_PREFIX + stats.getNoteId();
        redis.opsForHash().put(key, "views", String.valueOf(stats.getViews()));
        redis.opsForHash().put(key, "likes", String.valueOf(stats.getLikes()));
        redis.opsForHash().put(key, "favorites", String.valueOf(stats.getFavorites()));
        redis.opsForHash().put(key, "comments", String.valueOf(stats.getComments()));
        redis.opsForHash().put(key, "updatedAt", stats.getUpdatedAt() != null ? stats.getUpdatedAt().toString() : "");
        redis.opsForHash().put(key, "version", String.valueOf(stats.getVersion()));
        redis.expire(key, java.time.Duration.ofDays(7)); // 设置缓存过期
    }

    /**
     * Redis Hash → DO
     */
    private NoteStatsDO mapToStats(Long id, Map<Object, Object> map) {
        NoteStatsDO stats = new NoteStatsDO();
        stats.setNoteId(id);
        stats.setViews(Long.parseLong(map.getOrDefault("views", "0").toString()));
        stats.setLikes(Long.parseLong(map.getOrDefault("likes", "0").toString()));
        stats.setFavorites(Long.parseLong(map.getOrDefault("favorites", "0").toString()));
        stats.setComments(Long.parseLong(map.getOrDefault("comments", "0").toString()));

        String updatedStr = map.getOrDefault("updatedAt", "").toString();
        stats.setUpdatedAt(updatedStr.isEmpty() ? null : LocalDateTime.parse(updatedStr));

        stats.setVersion(Long.parseLong(map.getOrDefault("version", "0").toString()));

        return stats;
    }

    private NoteStatsDO defaultStats(Long noteId) {
        NoteStatsDO stats = new NoteStatsDO();
        stats.setNoteId(noteId);
        stats.setViews(0L);
        stats.setLikes(0L);
        stats.setFavorites(0L);
        stats.setComments(0L);
        stats.setVersion(0L);
        stats.setUpdatedAt(null);
        return stats;
    }

    // 最近更新时间评分
    private double recencyScore(LocalDateTime updatedAt) {
        if (updatedAt == null) return 0;
        long millis = java.time.Duration.between(updatedAt, LocalDateTime.now()).toMillis();
        double days = millis / 86400000.0;
        return 1.0 / (days + 1.0);
    }

    private static class ScoredNote {
        NoteSearchVO vo;
        double score;
        LocalDateTime updatedAt;

        ScoredNote(NoteSearchVO vo, double score) {
            this.vo = vo;
            this.score = score;
        }
    }
}
