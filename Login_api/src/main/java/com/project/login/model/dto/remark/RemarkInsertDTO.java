package com.project.login.model.dto.remark;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RemarkInsertDTO {

    private Long noteId; // 评论属于哪篇笔记

    private Long userId; // 评论用户

    private String username;

    private String replyToUsername;

    private String content; // 评论内容

    private String parentId; // 父评论（楼中楼）

    private Boolean isReply;

    private String replyToRemarkId;
}
