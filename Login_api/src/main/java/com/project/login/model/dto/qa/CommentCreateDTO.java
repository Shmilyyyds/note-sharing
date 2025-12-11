package com.project.login.model.dto.qa;

import lombok.Data;

@Data
public class CommentCreateDTO {
    private String questionId;
    private Long answerId;
    private Long authorId;
    private String content;
}


