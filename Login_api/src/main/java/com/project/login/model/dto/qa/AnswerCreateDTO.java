package com.project.login.model.dto.qa;

import lombok.Data;

@Data
public class AnswerCreateDTO {
    private String questionId;
    private Long authorId;
    private String content;
}


