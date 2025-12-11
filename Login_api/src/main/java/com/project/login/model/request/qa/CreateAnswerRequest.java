package com.project.login.model.request.qa;

import lombok.Data;

@Data
public class CreateAnswerRequest {
    private String questionId;
    private Long authorId;
    private String content;
}

