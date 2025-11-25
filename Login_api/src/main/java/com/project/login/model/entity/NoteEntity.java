package com.project.login.model.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(indexName = "notes")
public class NoteEntity {

    @Id
    private Long id; // MySQL note.id

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String contentSummary; // 对应 content_summary

}
