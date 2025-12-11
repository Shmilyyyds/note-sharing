package com.project.login.model.vo;

import lombok.Data;

@Data
public class NoteStatsVO {
    private Long noteId;
    private String authorName;
    private Long views;
    private Long likes;
    private Long favorites;
    private Long comments;
}
