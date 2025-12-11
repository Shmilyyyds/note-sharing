package com.project.login.model.dataobject;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "questions")
public class QuestionDO {

    @Id
    private String questionId;

    private Long authorId;
    private String title;
    private String content;
    private List<String> tags = new ArrayList<>();
    private LocalDateTime createdAt;              // 提问时间

    /** 提问的点赞列表 */
    private List<Long> likes = new ArrayList<>();

    /** 提问的收藏列表 */
    private List<Long> favorites = new ArrayList<>();

    /** 回答列表 */
    private List<AnswerDO> answers = new ArrayList<>();

    @Data
    public static class AnswerDO {
        private Long answerId;
        private Long authorId;
        private String content;
        private LocalDateTime createdAt;          // 回答时间

        /** 回答点赞 */
        private List<Long> likes = new ArrayList<>();

        /** 一级评论 */
        private List<CommentDO> comments = new ArrayList<>();
    }

    @Data
    public static class CommentDO {
        private Long commentId;
        private Long authorId;
        private String content;
        private LocalDateTime createdAt;          // 评论时间

        /** 评论点赞 */
        private List<Long> likes = new ArrayList<>();

        /** 评论下的回复（二级评论） */
        private List<ReplyDO> replies = new ArrayList<>();
    }

    @Data
    public static class ReplyDO {
        private Long replyId;
        private Long authorId;
        private String content;
        private LocalDateTime createdAt;          // 回复时间

        /** 回复点赞 */
        private List<Long> likes = new ArrayList<>();
    }
}
