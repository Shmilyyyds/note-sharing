package com.project.login.service.recommend;

import com.project.login.model.vo.NoteSearchVO;
import com.project.login.model.dto.search.NoteSearchDTO;
import com.project.login.service.search.SearchService;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserProfileQueryService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final SearchService searchService;

    public UserProfileQueryService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper,
                                   SearchService searchService) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.searchService = searchService;
    }

    private List<String> getTopKeywords(Long userId, int topN) throws Exception {
        String json = redisTemplate.opsForValue().get("user_fused_profile:" + userId);
        if (json == null) return Collections.emptyList();

        Map<String, Double> fused = objectMapper.readValue(json, Map.class);
        return fused.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 根据用户画像关键词推荐笔记，并去重
     */
    public List<NoteSearchVO> recommendNotesByKeywords(Long userId, int topN) throws Exception {
        List<String> keywords = getTopKeywords(userId, topN);
        if (keywords.isEmpty()) return Collections.emptyList();

        Map<Long, NoteSearchVO> uniqueNotes = new LinkedHashMap<>();

        for (String keyword : keywords) {
            NoteSearchDTO dto = new NoteSearchDTO();
            dto.setKeyword(keyword);
            List<NoteSearchVO> results = searchService.searchNotes(dto);

            for (NoteSearchVO vo : results) {
                // 根据 noteId 去重，保留第一次出现的
                uniqueNotes.putIfAbsent(vo.getNoteId(), vo);
            }
        }

        // 综合排序：只考虑统计数据
        List<NoteSearchVO> merged = new ArrayList<>(uniqueNotes.values());
        merged.sort((a, b) -> {
            double scoreA = a.getViewCount() + a.getLikeCount() + a.getFavoriteCount() + a.getCommentCount();
            double scoreB = b.getViewCount() + b.getLikeCount() + b.getFavoriteCount() + b.getCommentCount();
            return Double.compare(scoreB, scoreA); });

        return merged;
    }
}
