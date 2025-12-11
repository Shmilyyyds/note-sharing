package com.project.login.model.vo.qa;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO {
    private Long commentId;
    private Long authorId;
    private String content;
    private LocalDateTime createdAt;
    private int likeCount;

    private List<ReplyVO> replies;
}


