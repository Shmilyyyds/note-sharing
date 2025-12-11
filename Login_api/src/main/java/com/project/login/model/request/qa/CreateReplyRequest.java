package com.project.login.model.request.qa;

import lombok.Data;

@Data
public class CreateReplyRequest {
    private String questionId;
    private Long answerId;
    private Long commentId;
    private Long authorId;
    private String content;
}

