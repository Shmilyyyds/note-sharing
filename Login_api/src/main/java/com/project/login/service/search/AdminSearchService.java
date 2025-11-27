package com.project.login.service.search;

import com.project.login.model.dto.search.NoteSearchDTO;
import com.project.login.model.vo.NoteSearchVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminSearchService {

    private final SearchService searchService;
    private final RedisTemplate<String, Object> redisTemplate;

    public List<NoteSearchVO> searchNotes(NoteSearchDTO dto) {
        String key = "admin:search:" + dto.getKeyword();
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<NoteSearchVO> v = (List<NoteSearchVO>) cached;
            return v;
        }
        List<NoteSearchVO> results = searchService.searchNotes(dto);
        redisTemplate.opsForValue().set(key, results, Duration.ofMinutes(30));
        return results;
    }
}
