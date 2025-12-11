package com.project.login.model.dto.notestats;

import java.io.Serializable;
import lombok.Data;

@Data
public class NoteStatsDTO implements Serializable {
    private Long noteId;
    private Long views;
    private Long likes;
    private Long favorites;
    private Long comments;
}
