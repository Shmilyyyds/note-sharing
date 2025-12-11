package com.project.login.model.request.remark;



import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class RemarkInsertRequest {

    @NotNull(message = "noteId cannot be null")
    private Long noteId; // 评论属于哪篇笔记


    @NotNull(message = "userId cannot be null")
    private Long userId; // 评论用户


    @NotBlank(message = "content cannot be blank")
    private String content; // 评论内容


    private Long parentId; // 父评论（楼中楼）

    @NotNull(message = "isReceive cannot be null")
    private Boolean isReceive;

    private Long receiveToRemarkId; // 回复的目标评论（业务字段，可为空）
}