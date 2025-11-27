package com.project.login.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class NoteSearchVO {

    private Long noteId;

    private String title;
    private String contentSummary; // content_summary

    private String authorName;

    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;
    
    private LocalDateTime updatedAt;

}
