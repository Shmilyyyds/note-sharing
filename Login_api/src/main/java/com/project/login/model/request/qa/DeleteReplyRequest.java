package com.project.login.model.request.qa;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class DeleteReplyRequest {

    @NotBlank(message = "Question ID is required")
    private String questionId;  // 问题ID

    @NotNull(message = "Answer ID is required")
    private Long answerId;    // 回答ID

    @NotNull(message = "Comment ID is required")
    private Long commentId;   // 评论ID

    @NotNull(message = "Reply ID is required")
    private Long replyId;     // 回复ID
}

