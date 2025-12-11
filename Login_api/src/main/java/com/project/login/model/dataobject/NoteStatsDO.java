package com.project.login.model.dataobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteStatsDO {

    private Long noteId;

    private String authorName;

    private Long views;
    private Long likes;
    private Long favorites;
    private Long comments;

    private LocalDateTime lastActivityAt;
    private Long version;

    private LocalDateTime updatedAt;
}
