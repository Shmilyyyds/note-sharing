package com.project.login.controller;

import com.project.login.model.response.StandardResponse;
import com.project.login.model.vo.NoteSearchVO;
import com.project.login.service.hot.HotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/hot")
@RequiredArgsConstructor
public class HotController {

    private final HotService hotService;

    /**
     * 获取热门笔记列表
     * @return StandardResponse 包装的热门笔记 VO 列表
     */
    @GetMapping("/notes")
    public StandardResponse<List<NoteSearchVO>> getHotNotes() {
        List<NoteSearchVO> hotNotes = hotService.getHotNotesDetail();
        return StandardResponse.success(hotNotes);
    }
}
