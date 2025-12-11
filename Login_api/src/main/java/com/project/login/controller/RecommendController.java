package com.project.login.controller;

import com.project.login.model.response.StandardResponse;
import com.project.login.model.vo.NoteSearchVO;
import com.project.login.model.vo.qa.QuestionVO;
import com.project.login.service.recommend.UserProfileQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final UserProfileQueryService userProfileQueryService;

    @GetMapping("/notes")
    public StandardResponse<List<NoteSearchVO>> recommendNotes(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "topN", defaultValue = "10") int topN) throws Exception {
        List<NoteSearchVO> recommendedNotes = userProfileQueryService.recommendNotesByKeywords(userId, topN);

        // 成功返回推荐列表
        return StandardResponse.success(recommendedNotes);

    }

    @GetMapping("/QAs")
    public StandardResponse<List<QuestionVO>> recommendQAs(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "topN", defaultValue = "10") int topN) throws Exception {
        List<QuestionVO> recommendedQAs = userProfileQueryService.recommendQuestionsByKeywords(userId, topN);

        // 成功返回推荐列表
        return StandardResponse.success(recommendedQAs);

    }
}
