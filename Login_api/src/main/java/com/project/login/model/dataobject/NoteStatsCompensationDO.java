package com.project.login.model.dataobject;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NoteStatsCompensationDO {

    private Long id;

    private Long noteId;

    private String authorName;

    private Long views;
    private Long likes;
    private Long favorites;
    private Long comments;

    private LocalDateTime lastActivityAt;

    private String status;
    private Integer retryCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
