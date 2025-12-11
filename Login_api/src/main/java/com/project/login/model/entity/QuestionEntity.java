package com.project.login.model.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@Document(indexName = "questions")
public class QuestionEntity {

    @Id
    private String questionId; // 对应 QuestionDO.questionId

    @Field(type = FieldType.Text)
    private String title;    // 问题标题

    @Field(type = FieldType.Text)
    private String content;  // 问题内容

    @Field(type = FieldType.Text)
    private String tags; // 标签合并（使用“，”分隔）
}

