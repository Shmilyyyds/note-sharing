package com.project.login.service.recommend;

import com.project.login.model.dataobject.QuestionDO;
import com.project.login.model.vo.NoteSearchVO;
import com.project.login.model.dto.search.NoteSearchDTO;
import com.project.login.model.vo.qa.QuestionVO;
import com.project.login.service.qa.QuestionService;
import com.project.login.service.search.SearchQAService;
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
    private final SearchQAService qaService;

    public UserProfileQueryService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper,
                                   SearchService searchService, SearchQAService qaService) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.searchService = searchService;
        this.qaService = qaService;
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

    /**
     * 根据用户画像关键词推荐问答（QA），并去重
     */
    public List<QuestionVO> recommendQuestionsByKeywords(Long userId, int topN) throws Exception {
        // 获取用户画像中的关键词
        List<String> keywords = getTopKeywords(userId, topN);
        if (keywords.isEmpty()) return Collections.emptyList();

        // 存储去重后的问题
        Map<String, QuestionVO> uniqueQuestions = new LinkedHashMap<>();

        // 遍历每个关键词进行搜索
        for (String keyword : keywords) {
            // 直接调用 SearchQAService 来执行关键词搜索
            List<QuestionVO> results = qaService.searchQuestions(keyword);

            // 对返回的问答进行去重
            for (QuestionVO vo : results) {
                uniqueQuestions.putIfAbsent(vo.getQuestionId(), vo);
            }
        }

        // 综合排序：只考虑统计数据（例如：回答数、点赞数等）
        List<QuestionVO> merged = new ArrayList<>(uniqueQuestions.values());
        merged.sort((a, b) -> {
            double scoreA = a.getAnswers().size() + a.getLikeCount() + a.getFavoriteCount();
            double scoreB = b.getAnswers().size() + b.getLikeCount() + b.getFavoriteCount();
            return Double.compare(scoreB, scoreA);
        });

        return merged;
    }


}
