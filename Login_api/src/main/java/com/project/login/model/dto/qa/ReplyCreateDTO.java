package com.project.login.model.dto.qa;

import lombok.Data;

@Data
public class ReplyCreateDTO {
    private String questionId;
    private Long answerId;
    private Long commentId;
    private Long authorId;
    private String content;
}

