package com.project.login.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class QuestionEvent {
    private String questionId;    // Mongo 主键
    private String title;         // 问题标题
    private String content;       // 问题内容
    private String tags;          // 标签
    private EventType type;       // CREATE 或 DELETE

    public enum EventType {
        CREATE, DELETE
    }
}

