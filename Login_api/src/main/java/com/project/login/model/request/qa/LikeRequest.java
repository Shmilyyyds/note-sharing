package com.project.login.model.request.qa;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LikeRequest {
    @NotBlank(message = "Question ID is required")
    private String questionId; // 可为空
    private Long answerId;
    private Long commentId;
    private Long replyId;
    private Long userId;
}

