package com.project.login.model.request.qa;

import lombok.Data;

@Data
public class CreateCommentRequest {
    private String questionId;
    private Long answerId;
    private Long authorId;
    private String content;
}


