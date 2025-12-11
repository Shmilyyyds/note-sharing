package com.project.login.model.request.qa;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class DeleteAnswerRequest {

    @NotBlank(message = "Question ID is required")
    private String questionId;  // 问题ID

    @NotNull(message = "Answer ID is required")
    private Long answerId;    // 回答ID
}

