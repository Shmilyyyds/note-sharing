package com.project.login.model.event;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EsNoteEvent {

    private Long noteId;

    private NoteActionType action; // CREATE / UPDATE / DELETE

    private String title;
    private String contentSummary; // ES 搜索用摘要

    // 更新时间
    private LocalDateTime updatedAt;
}
