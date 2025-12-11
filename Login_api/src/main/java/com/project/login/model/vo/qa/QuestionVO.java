package com.project.login.model.vo.qa;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuestionVO {
    private String questionId;
    private Long authorId;
    private String title;
    private String content;
    private List<String> tags;

    private LocalDateTime createdAt;

    private Integer likeCount;
    private Integer favoriteCount;

    private List<AnswerVO> answers;
}



