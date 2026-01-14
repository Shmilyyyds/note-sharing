package com.project.login.model.vo.qa;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReplyVO {
    private Long replyId;
    private Long authorId;
    private String authorName;  // 回复者用户名
    private String content;
    private LocalDateTime createdAt;

    private Integer likeCount;
}


