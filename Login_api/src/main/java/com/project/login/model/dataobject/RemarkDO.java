package com.project.login.model.dataobject;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection="remark")
public class RemarkDO {
    @Id// MongoDB 中的主键字段为 id
    private String _id; // MongoDB 文档主键，对应 id

    @Field("note_id") // 将 noteId 映射为 note_id
    @Indexed
    private Long noteId; // 属于哪篇笔记

    @Field("user_id") // 将 userId 映射为 user_id
    private Long userId; // 评论发布者

    @Field("username")
    private String username;

    @Field("content") // 映射为 content
    private String content; // 评论内容

    @Field("created_at") // 映射为 created_at
    private String createdAt; // 创建时间

    @Field("parent_id") // 映射为 parent_id
    private String parentId; // 上一级评论（楼中楼）

    @Field("is_receive") // 映射为 is_receive
    private Boolean isReceive; // 是否是回复

    @Field("receive_to_remark_id") // 映射为 receive_to_remark_id
    private String receiveToRemarkId; // 回复哪条评论

    @Field("reply_to_user_name")
    private String replyToUsername;


}
