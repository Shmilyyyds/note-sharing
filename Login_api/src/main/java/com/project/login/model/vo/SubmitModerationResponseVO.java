package com.project.login.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 提交审查响应VO
 * 包含审查ID、内容类型、内容ID、状态、创建时间
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitModerationResponseVO {
    private Long moderationId;      // 审查ID
    private String contentType;     // 内容类型（如：NOTE）
    private Long contentId;         // 内容ID（笔记ID）
    private String status;          // 状态（SAFE, FLAGGED, ERROR）
    private LocalDateTime createdAt; // 创建时间
}
