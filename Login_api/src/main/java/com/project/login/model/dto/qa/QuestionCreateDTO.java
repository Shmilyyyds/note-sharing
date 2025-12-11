package com.project.login.model.dto.qa;

import lombok.Data;

import java.util.List;

@Data
public class QuestionCreateDTO {
    private Long authorId;
    private String title;
    private String content;
    private List<String> tags;
}



