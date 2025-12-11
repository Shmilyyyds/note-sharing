package com.project.login.model.request.qa;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FavoriteRequest {
    @NotBlank(message = "Question ID is required")
    private String questionId;
    private Long userId;
}
