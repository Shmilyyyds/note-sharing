package com.project.login.model.request.qa;

import lombok.Data;

import java.util.List;

@Data
public class CreateQuestionRequest {
    private Long authorId;
    private String title;
    private String content;
    private List<String> tags;
}
